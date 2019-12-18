package com.xuecheng.manage_media_process;

import com.xuecheng.framework.utils.Mp4VideoUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @version 1.0
 * @create 2018-07-12 9:11
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestProcessBuilder {

    //使用processBuilder来调用第三方应用程序
    @Test
    public void testProcessBuilder() throws IOException {
        //创建processBuilder对象
        ProcessBuilder processBuilder = new ProcessBuilder();
        //设置第三方应用程序
        List<String> command = new ArrayList<>();
        command.add("F:\\bangong\\Ffmpeg\\ffmpeg-20180227-fa0c9d6-win64-static\\bin\\ffmpeg.exe");
        command.add("-i");
        command.add("D:\\临时\\video\\0\\lucene.avi");
        command.add("-y"); //覆盖输出文件
        command.add("-c:v");
        command.add("libx264");
        command.add("-s");
        command.add("1280*720");
        command.add("-pix_fmt");
        command.add("yuv420p");
        command.add("-b:a");
        command.add("63k");
        command.add("-b:v");
        command.add("753k");
        command.add("-r");
        command.add("18");
        command.add("D:\\临时\\video\\0\\lucene.mp4");
        processBuilder.command(command);
        //将标准的输入流和错误输入流合并
        processBuilder.redirectErrorStream(true);
        //启动进程
        Process process = processBuilder.start();

        //通过标准输入流来拿到正常和错误的信息
        InputStream inputStream = process.getInputStream();

        //转成字符流
        InputStreamReader reader = new InputStreamReader(inputStream, "gbk");
        //缓冲
        char b[] = new char[1024];
        int len = -1;
        while ((len = reader.read(b)) != -1) {
            String s = new String(b, 0, len);
            System.out.println(s);
        }
        inputStream.close();
        reader.close();
    }

    @Test
    public void testMp4VideoUtil() {
        String ffmpeg_path = "F:\\bangong\\Ffmpeg\\ffmpeg-20180227-fa0c9d6-win64-static\\bin\\ffmpeg.exe";
        String video_path = "D:\\临时\\video\\0\\lucene.avi";
        String mp4_name = "lucene.mp4";
        String mp4folder_path = "D:\\临时\\video\\0\\";
        Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpeg_path, video_path, mp4_name, mp4folder_path);
        //生成MP4
        String result = mp4VideoUtil.generateMp4();
        System.out.println(result);
    }

}
