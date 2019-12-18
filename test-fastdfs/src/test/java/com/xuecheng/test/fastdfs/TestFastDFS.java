package com.xuecheng.test.fastdfs;


import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFastDFS {

    //上传文件
    @Test
    public void testUpload() {
        //加载配置文件
        try {
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            //定义 trackerClient 用于请求trackerServer
            TrackerClient trackerClient = new TrackerClient();
            //连接Tracker
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取stroage
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
            //创建Storage的客户端
            StorageClient1 storageClient1 = new StorageClient1(trackerServer, storeStorage);
            //上传文件
            //文件路径
            String filePath = "D:/临时/test.jpg";
            String fileID = storageClient1.upload_appender_file1(filePath, "jpg", null);
            System.out.println(fileID);
            //group1/M00/00/00/wKiahl3SFIiENuPeAAAAAKXZOcg492.jpg
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
    }

    //下载文件
    @Test
    public void testDownload() {
        //加载配置文件
        try {
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            //定义 trackerClient 用于请求trackerServer
            TrackerClient trackerClient = new TrackerClient();
            //连接Tracker
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取stroage
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
            //创建Storage的客户端
            StorageClient1 storageClient1 = new StorageClient1(trackerServer, storeStorage);
            //下载文件
            byte[] bytes = storageClient1.download_file1("group1/M00/00/00/wKiahl3SOuGEHYMPAAAAAKXZOcg238.jpg");
            //使用输出流保存文件
            FileOutputStream fileOutputStream = new FileOutputStream(new File("D:/临时/test1.jpg"));
            fileOutputStream.write(bytes);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
    }
}
