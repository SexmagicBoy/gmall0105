package com.atguigu.gmall.manage;

import constant.UrlConstant;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManageWebApplicationTests {

    @Test
    public void contextLoads() throws IOException, MyException {
        // 获取配置文件路径，初始化 fastDFS
        String tracker = GmallManageWebApplicationTests.class.getResource("/tracker.conf").getPath();
        ClientGlobal.init(tracker);

        // 获取一个 trackerServer 的实例
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getTrackerServer();

        // 通过 tracker 获取 storage 客户端
        StorageClient storageClient = new StorageClient(trackerServer, null);
        String[] uploadInfos = storageClient.upload_file("E:\\OneDrive\\图片\\头像\\v2-1e440afa4e5178318f1cc7542366f889_720w.jpg",
                "jpg", null);

        StringBuilder imageUrl = new StringBuilder(UrlConstant.FAST_DFS_URL);

        for (String uploadInfo : uploadInfos) {
            imageUrl.append("/");
            imageUrl.append(uploadInfo);
        }

        System.out.println(imageUrl);
        // http://192.168.200.100/group1/M00/00/00/wKjIZGN7GFqAKnKXAACP1XSSbZY530.jpg
    }

}
