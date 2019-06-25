package com.example.sx.practicalassistant.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.example.sx.practicalassistant.R;
import com.example.sx.practicalassistant.app.BaseApplication;
import com.example.sx.practicalassistant.bean.ChatMessage;
import com.example.sx.practicalassistant.bean.Command;
import com.example.sx.practicalassistant.bean.Message;
import com.example.sx.practicalassistant.bean.PlayBean;

import java.io.InputStream;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by SX on 2017/2/13.
 */
public class CommandParse {
    private static String numberregex="\\d+";
    private static Message message=new Message();
    private static String lastType=Command.UNKNOW;
    public static Command parse(String command){
        switch (lastType){
            case Command.SENDMESSAGE1:
                message.setContent(command);
                lastType=Command.UNKNOW;
                return new Command(Command.SENDMESSAGE2,message);
            case Command.ROMANISATIONOFCHINESE1:
                lastType=Command.UNKNOW;
                return new Command(Command.ROMANISATIONOFCHINESE2,command);
        }
        if(command.startsWith("打开")){
            return new Command(Command.OPENAPP,command.substring("打开".length()));
        }else if(command.startsWith("呼叫")||command.startsWith("打电话给")){
            String number="";
            if(command.startsWith("呼叫")){
                number=command.substring("呼叫".length());
                if(!number.matches(numberregex)){
                    return new Command(Command.UNKNOW,"呼叫手机号包含非数字："+number);
                }
            } else{
                String name = command.substring("打电话给".length());
                Command phoneContacts = getPhoneContacts(name);
                if(phoneContacts.getType()==null)phoneContacts.setType(Command.CALLPHONE);
                return phoneContacts;
//                ContentResolver contentResolver = BaseApplication.getContext().getContentResolver();
//                Cursor cursor = contentResolver.query(android.provider.ContactsContract.Contacts.CONTENT_URI,
//                        new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
//                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+"= ?", new String[]{name}, null);
//                if(cursor.moveToFirst()){
//                    number=cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//                }else{
//                    return new Command(Command.UNKNOW,"为查询到"+name+"的号码");
//                }
            }
            if(!TextUtils.isEmpty(number))
            return new Command(Command.CALLPHONE,number);
            else
                return new Command(Command.CALLPHONE,"号码不能为空");
        }else  if(command.startsWith("发短信给")){
            message.clear();
            String substring = command.substring("发短信给".length());
            if(substring.matches(numberregex)){
                message.setNumber(substring);
            }else{
                Command phoneContacts = getPhoneContacts(substring);
                if(phoneContacts.getType()==null){
                    message.setNumber(((String) phoneContacts.getData()));
                    message.setName(substring);
                }else{
                    return phoneContacts;
                }
            }
            lastType=Command.SENDMESSAGE1;
            return new Command(Command.SENDMESSAGE1,message);
        }else if(command.startsWith("生字查询")){
            lastType=Command.ROMANISATIONOFCHINESE1;
            return new Command(Command.ROMANISATIONOFCHINESE1,"请输入要查询的生字");
        }else if(command.startsWith("播放")){
            PlayBean playBean = new PlayBean();
            if(command.equals("播放音乐")){
                playBean.setType(PlayBean.AUDIO_TYPE);
            }else if(command.equals("播放视频")){
                playBean.setType(PlayBean.VIDEO_TYPE);
            }else{
                if(command.startsWith("播放视频")){
                    playBean.setType(PlayBean.VIDEO_TYPE);
                    playBean.setFileName(command.substring("播放视频".length()));
                }else{
                    playBean.setType(PlayBean.AUDIO_TYPE);
                    if(command.startsWith("播放音乐"))
                    playBean.setFileName(command.substring("播放音乐".length()));
                    else
                        playBean.setFileName(command.substring("播放".length()));
                }
            }
            return new Command(Command.PLAY,playBean);
        }else if(command.startsWith("计算")){
            return new Command(Command.CALCULATOR,command.substring("计算".length()));
        }
        return new Command(Command.UNKNOW,"不明白您的意思！");
    }


    public static String getLastType() {
        return lastType;
    }

    public static void setLastType(String lastType) {
        CommandParse.lastType = lastType;
    }

    /**得到手机通讯录联系人信息**/
    private static final String[] PHONES_PROJECTION = new String[] {
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Photo.PHOTO_ID, ContactsContract.CommonDataKinds.Phone.CONTACT_ID};
    /**联系人显示名称**/
    private static final int PHONES_DISPLAY_NAME_INDEX=0;
    /**电话号码**/
    private static final int PHONES_NUMBER_INDEX=1;
    private static Command getPhoneContacts(String name) {
        ContentResolver resolver = BaseApplication.getContext().getContentResolver();

        // 获取手机联系人  
        Cursor phoneCursor=resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,PHONES_PROJECTION,null,null,null);

        if(phoneCursor!=null)
            while(phoneCursor.moveToNext()){
                //得到手机号码  
                String phoneNumber=phoneCursor.getString(PHONES_NUMBER_INDEX);
                //当手机号码为空的或者为空字段 跳过当前循环  
                if(TextUtils.isEmpty(phoneNumber))
                continue;
                //得到联系人名称  
                String contactName=phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);
                if(name.equalsIgnoreCase(contactName)){
                    phoneCursor.close();
                    return new Command(null,phoneNumber);
                }
        }
        phoneCursor.close();
        Command simContacts = getSIMContacts(name);
        if(simContacts!=null)
            return simContacts;
        return new Command(Command.UNKNOW,"未查询到"+name+"的号码");
    }
    /**得到手机SIM卡联系人人信息**/
    private static Command getSIMContacts(String name){
        ContentResolver resolver=BaseApplication.getContext().getContentResolver();
        // 获取Sims卡联系人  
        Uri uri=Uri.parse("content://icc/adn");
        Cursor phoneCursor=resolver.query(uri,PHONES_PROJECTION,null,null,
        null);

        if(phoneCursor!=null){
            while(phoneCursor.moveToNext()){

                // 得到手机号码  
                String phoneNumber =phoneCursor.getString(PHONES_NUMBER_INDEX);
                // 当手机号码为空的或者为空字段 跳过当前循环  
                if(TextUtils.isEmpty(phoneNumber))
                continue;
                // 得到联系人名称  
                String contactName=phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);
                if(name.equalsIgnoreCase(contactName)){
                    phoneCursor.close();
                    return new Command(null,phoneNumber);
                }
                }
            }
        phoneCursor.close();
        return null;
    }
    private static   String decode(String s) {
        Pattern reUnicode = Pattern.compile("\\\\u([0-9a-zA-Z]{4})");
        Matcher m = reUnicode.matcher(s);
        StringBuffer sb = new StringBuffer(s.length());
        while (m.find()) {
            m.appendReplacement(sb,
                    Character.toString((char) Integer.parseInt(m.group(1), 16)));
        }
        m.appendTail(sb);
        return sb.toString();
    }
    private static   String encode(String s) {
        StringBuilder sb = new StringBuilder(s.length() * 3);
        for (char c : s.toCharArray()) {
            if (c < 256) {
                sb.append(c);
            } else {
                sb.append("\\u");
                sb.append(Character.forDigit((c >>> 12) & 0xf, 16));
                sb.append(Character.forDigit((c >>> 8) & 0xf, 16));
                sb.append(Character.forDigit((c >>> 4) & 0xf, 16));
                sb.append(Character.forDigit((c) & 0xf, 16));
            }
        }
        return sb.toString();
    }
}
