package util;

import constant.UrlConstant;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

// fastDFS 上传工具类
public class UploadUtil {
    // 获取 tracker 配置文件路径
    private static final String TRACKER = UploadUtil.class.getResource("/tracker.conf").getPath();

    // 初始化 fastDFS
    static {
        try {
            ClientGlobal.init(TRACKER);
        } catch (IOException | MyException e) {
            e.printStackTrace();
        }
    }

    public static String uploadImage(MultipartFile multipartFile) {

        String[] uploadInfos = null;

        try {
            // 获取 trackerServer 的实例
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getTrackerServer();

            // 通过 tracker 获取 storage 客户端
            StorageClient storageClient = new StorageClient(trackerServer, null);

            // 获取名称，截取后缀
            String originalFilename = multipartFile.getOriginalFilename();
            String extName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            byte[] bytes = multipartFile.getBytes();

            // 上传图片
            uploadInfos = storageClient.upload_file(bytes, extName, null);
        } catch (IOException | MyException e) {
            e.printStackTrace();
        }

        // 拼接生成的图片访问链接
        StringBuilder imageUrl = new StringBuilder(UrlConstant.FAST_DFS_URL);
        if (uploadInfos != null) {
            for (String uploadInfo : uploadInfos) {
                imageUrl.append("/");
                imageUrl.append(uploadInfo);
            }
        }

        return imageUrl.toString();
    }
}
