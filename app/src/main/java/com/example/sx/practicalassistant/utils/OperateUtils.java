package com.example.sx.practicalassistant.utils;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.sx.practicalassistant.app.BaseApplication;
import com.example.sx.practicalassistant.bean.Command;
import com.example.sx.practicalassistant.bean.Message;
import com.example.sx.practicalassistant.bean.PlayBean;
import com.example.sx.practicalassistant.bean.Response;
import com.example.sx.practicalassistant.db.DBOpenHelper;
import com.example.sx.practicalassistant.db.PlayBeanDB;
import com.iflytek.cloud.LexiconListener;
import com.iflytek.cloud.SpeechError;

import java.util.List;

import static com.iflytek.cloud.VerifierResult.TAG;

/**
 * Created by SX on 2017/3/3.
 */

public class OperateUtils {
    public static Response response=new Response();
    //标记指令执行结果状态，4种为完成，成功，失败，完成，
    //成功用于生字查询后关闭输入法，完成用于打电话、发短信界面跳转后关闭VoiceActivity
    public static int flag=Response.UNFINISH;
    public static Response operateCommand(@NonNull Command command, Context context) {
        response.clear();
        String result = "";
        String speak="";
         flag=Response.UNFINISH;
        switch (command.getType()) {
            case Command.OPENAPP:
                result = openApp(command, context);
                break;
            case Command.CALLPHONE:
                result = callPhone(command, context);
                break;
            case Command.SENDMESSAGE1:
                speak="短信内容是";
                Message data = (Message) command.getData();
                result="收件人："+(TextUtils.isEmpty(data.getName())?data.getNumber():data.getName());
                break;
            case Command.SENDMESSAGE2:
                Message message = (Message) command.getData();
                try {
                    Log.e(TAG, "operateCommand: message"+message.getNumber()+":  "+message.getContent() );
                    result = doSendSMSTo(message.getNumber(), message.getContent(),context);
//                    result=doSendSMSTo(message.getNumber(),message.getContent(),context);
                } catch (Exception e) {
                    result="发送失败";
                }
                break;
            case Command.ROMANISATIONOFCHINESE1:
                result= ((String) command.getData());
                break;
            case Command.ROMANISATIONOFCHINESE2:
                try {
                    result = getTranscription(((String) command.getData()), context);
                    if(TextUtils.isEmpty(result)) {
                        result = "未查询到该字拼音";
                        speak="未查询到该字拼音，语音读作"+(String) command.getData();
                        flag=Response.FAIL;
                    }else{
                        speak="查询成功，语音读作"+(String) command.getData();
                        flag=Response.SUCCESS;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    result = "未查询到该字拼音 ";
                    speak="未查询到该字拼音，语音读作"+(String) command.getData();
                    flag=Response.FAIL;
                }
                break;
            case Command.PLAY:
                result=play(((PlayBean) command.getData()),context);
                break;
            case Command.CALCULATOR:
                String calc = calc(((String) command.getData()));
                if(calc.startsWith("false")){
                    result=speak=calc.substring("false".length());
                    flag=Response.FAIL;
                }else{
                    speak="计算结果为"+calc;
                    result=calc;
                    flag=Response.SUCCESS;
                }
                break;
            case Command.UNKNOW:
                result = (String) command.getData();
                break;
        }
        response.setShowContent(result);
        response.setSpeakContent(speak);
        response.setFlag(flag);
        return response;
    }
    private static String calc(String data){
        data=data.replaceAll("(加|加上)","+");
        data=data.replaceAll("减去|减","-");
        data=data.replaceAll("乘以|乘上|乘|×","*");
        data=data.replaceAll("除上|除以|除|÷","/");
        String[] split = data.split("[+\\*/-]");
        for (int j=0;j<split.length;j++) {
            String num=split[j];
            data=data.replace(num,"");
            if(!num.matches("\\d*")){
                StringBuilder sb=new StringBuilder();
                for (int i = 0; i < num.length(); i++) {
                    char c = num.charAt(i);
                    if(c>=48&&c<=57){
                        sb.append(c);
                        continue;
                    }
                    switch (c){
                        case '零':
                            sb.append('0');
                            break;
                        case '一':
                            sb.append('1');
                            break;
                        case '二':
                            sb.append('2');
                            break;
                        case '三':
                            sb.append('3');
                            break;
                        case '四':
                            sb.append('4');
                            break;
                        case '五':
                            sb.append('5');
                            break;
                        case '六':
                            sb.append('6');
                            break;
                        case '七':
                            sb.append('7');
                            break;
                        case '八':
                            sb.append('8');
                            break;
                        case '九':
                            sb.append('9');
                            break;
                        case '.':
                            sb.append('.');
                            break;
                        default:
                            return "false包含非数字文字";
                    }
                }
                split[j]=sb.toString();
            }
        }
        if(!data.matches("[+\\*/-]+")||data.length()!=split.length-1){
            return "false表达式运算符有误请正确录入";
        }
        double result=Double.valueOf(split[0]);
        for (int i = 0; i < split.length-1; ) {
            switch (data.charAt(i)){
                case '+':
                    result+=Double.valueOf(split[++i]);
                    break;
                case '-':
                    result-=Double.valueOf(split[++i]);
                    break;
                case '*':
                    result*=Double.valueOf(split[++i]);
                    break;
                case '/':
                    Double aDouble = Double.valueOf(split[++i]);
                    if(aDouble==0){
                        return "false除数不能为零";
                    }
                    result/= aDouble;
                    break;
            }
        }
        return ""+result;
    }
    private static String play(final PlayBean playBean, final Context context){
        final SharedPreferences play = context.getSharedPreferences("practicalassistant", Context.MODE_PRIVATE);
        final PlayBeanDB db = PlayBeanDB.getIntance(context);
        Cursor cursor = getCursor(playBean, play, db);
        if(cursor==null||(!cursor.moveToFirst())) {
            AudioUtils.scanFile(playBean.getFileName(),playBean.getType(), context, new AudioUtils.ScanListening() {
                @Override
                public void finsh(final String type, Long time) {
                    play.edit().putLong(type,time).apply();
                    Cursor cursor = getCursor(playBean, play, db);
                    if(cursor==null||(cursor.moveToFirst())){
                        do{
                            playBean.addPath(cursor.getString(cursor.getColumnIndex("path")),cursor.getString(cursor.getColumnIndex("name")));
                        }while(cursor.moveToNext());
                        cursor.close();
                        cursor=null;
                        new Thread(){
                            @Override
                            public void run() {
                                super.run();
                                AudioUtils.play(playBean,context);
                            }
                        }.start();
                        VoiceUtils.getIntance(context).startSpeak("开始播放");
                        new Thread(){
                            @Override
                            public void run() {
                                super.run();
                                BaseApplication.getBaseApplication().getWord(type,null);
                            }
                        }.start();
                    }else{
                        VoiceUtils.getIntance(context).startSpeak("未扫描到需要的文件");
                    }
                }
            });
            return "正在搜索文件";
        }else{
            AudioUtils.index++;
            do{
                playBean.addPath(cursor.getString(cursor.getColumnIndex("path")),cursor.getString(cursor.getColumnIndex("name")));
            }while(cursor.moveToNext());
            cursor.close();
            cursor=null;
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    AudioUtils.play(playBean,context);
                }
            }.start();
            return "开始播放";
        }
    }
//    private static LexiconListener lexiconListener=new LexiconListener(){
//        @Override
//        public void onLexiconUpdated(String lexiconId,SpeechError error){
//            if(error!=null){
//                Log.d(TAG,"上传"+error.toString());
//            }else{
//                Log.d(TAG, "上传成功！");
//            }
//        }
//    };
    private static Cursor getCursor(PlayBean playBean, SharedPreferences play, PlayBeanDB db) {
        Cursor cursor=null;
        switch (playBean.getType()){
            case PlayBean.AUDIO_TYPE:
                boolean b =true;
                try {
                    b = play.getLong(PlayBean.AUDIO_TYPE, 0l) == 0;
                }catch (Exception e){
                }
                if(!b) {
                    if (TextUtils.isEmpty(playBean.getFileName())) {
                        cursor = db.find(new String[]{"name", "path"}, "type = ?", new String[]{PlayBean.AUDIO_TYPE});
                    } else {
                        cursor = db.find(new String[]{"name",  "path"}, "type = ? and name like ?", new String[]{PlayBean.AUDIO_TYPE, "%"+playBean.getFileName()+"%"});
                    }
                }
                break;
            case PlayBean.VIDEO_TYPE:
                boolean flag =true;
                try {
                    flag = play.getLong(PlayBean.VIDEO_TYPE, 0l) == 0;
                }catch (Exception e){
                }
                if(!flag) {
                    if (TextUtils.isEmpty(playBean.getFileName())) {
                        cursor = db.find(new String[]{"name",  "path"}, "type = ?", new String[]{PlayBean.VIDEO_TYPE});
                    } else {
                        cursor = db.find(new String[]{"name",  "path"}, "type = ? and name like ?", new String[]{PlayBean.VIDEO_TYPE,"%"+playBean.getFileName()+"%"});
                    }
                }
                break;
        }
        return cursor;
    }

    private static String getTranscription(String word,Context context) throws  Exception{
        String transcription = WordsDBUtils.getTranscription(context, word);
        return transcription;
    }
    private static String doSendSMSTo(String phoneNumber,String message,Context context)throws Exception{
        Toast.makeText(context,"手机号："+phoneNumber,Toast.LENGTH_SHORT).show();
        if(PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)){
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+phoneNumber));
            intent.putExtra("sms_body",message);
            context.startActivity(intent);
            return "正在启动短信";
            }
        return "号码格式不对";
        }
    private static String sendSMS(String phoneNumber,String message,Context context) throws Exception{
        //获取短信管理器   
        android.telephony.SmsManager smsManager=android.telephony.SmsManager.getDefault();
        //拆分短信内容（手机短信长度限制） 
        //处理发送状态
        String SENT_SMS_ACTION="SENT_SMS_ACTION";
        Intent sentIntent=new Intent(SENT_SMS_ACTION);
        PendingIntent sentPI=PendingIntent.getBroadcast(context,0,sentIntent, 0);
        //处理返回的接收状态   
        String DELIVERED_SMS_ACTION="DELIVERED_SMS_ACTION";
// create the deilverIntent parameter  
        Intent deliverIntent=new Intent(DELIVERED_SMS_ACTION);
        PendingIntent deliverPI=PendingIntent.getBroadcast(context,0,
                deliverIntent,0);
        List<String>divideContents=smsManager.divideMessage(message);
        for(String text:divideContents){
        smsManager.sendTextMessage(phoneNumber,null,text,sentPI,deliverPI);
        }
        return "正在发送中";
        }
    private static String callPhone(Command command, Context context) {
        Intent dialIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + ((String) command.getData())));
        try {
            context.startActivity(dialIntent);
        }catch (Exception e){
            return "启动呼叫失败";
        }
        flag=Response.FINISH;
        return "正在呼叫中";
    }
    private static String openApp(Command command,Context context) {
        String appName=((String) command.getData());
        String result="";
        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(0);
        String packageName="";
        for (ApplicationInfo installedApplication : installedApplications) {
            String appname = installedApplication.loadLabel(packageManager).toString();
            if(appname.equals(appName)) {
                 packageName = installedApplication.packageName;
                break;
            }
        }
        if(TextUtils.isEmpty(packageName)){
            for (ApplicationInfo installedApplication : installedApplications) {
                String appname = installedApplication.loadLabel(packageManager).toString();
                if(appname.contains(appName)) {
                    packageName = installedApplication.packageName;
                    break;
                }
            }
        }
        if(TextUtils.isEmpty(packageName)){
            result="未安装此应用！";
            return result;
        }
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageName);

        List<ResolveInfo> apps = packageManager.queryIntentActivities(resolveIntent, 0);

        ResolveInfo ri = apps.iterator().next();
        if (ri != null ) {
            String className = ri.activityInfo.name;

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            ComponentName cn = new ComponentName(packageName, className);

            intent.setComponent(cn);
            context.startActivity(intent);
            result="应用已启动！";
            flag=Response.FINISH;
        }else{
            result = "查询应用失败";
        }
        return result;
    }
}
