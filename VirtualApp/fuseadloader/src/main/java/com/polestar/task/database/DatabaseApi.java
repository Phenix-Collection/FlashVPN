package com.polestar.task.database;

import com.polestar.task.IProductStatusListener;
import com.polestar.task.ITaskStatusListener;
import com.polestar.task.database.datamodels.ProductInfo;
import com.polestar.task.database.datamodels.Task;
import com.polestar.task.database.datamodels.UserInfo;

import java.util.List;

public interface DatabaseApi {

    /**
     *
     * @return all tasks that available in server. App client should judge whether it is doable by task info
     */
    List<Task> getActiveTasks();

    List<Task> getActiveTasksByType(String type);

    Task getTaskById(String taskId);


    UserInfo getMyUserInfo();

    List<ProductInfo> getAllProductInfo();

    List<ProductInfo> getPurchasedProducts();

    ProductInfo getProductInfo(String id);

    void consumeProduct(String id, int amount, IProductStatusListener listener);

    void requestFinishTask(String taskId, ITaskStatusListener listener);

}
