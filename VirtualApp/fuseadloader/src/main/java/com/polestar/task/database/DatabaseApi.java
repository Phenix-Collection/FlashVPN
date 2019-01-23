package com.polestar.task.database;

import com.polestar.task.network.datamodels.Product;
import com.polestar.task.network.datamodels.Task;
import com.polestar.task.network.datamodels.User;

import java.util.ArrayList;
import java.util.List;

public interface DatabaseApi {

    /**
     *
     * @return all tasks that available in server. App client should judge whether it is doable by task info
     */
    List<Task> getActiveTasks();
    boolean setActiveTasks(ArrayList<Task> tasks);

    List<Task> getActiveTasksByType(int type);

    Task getTaskById(long taskId);

    User getMyUserInfo();
    boolean setUserInfo(User user);

    List<Product> getAllProductInfo();
    boolean setActiveProducts(ArrayList<Product> products);

//    List<ProductInfoNoUse> getPurchasedProducts();

    Product getProductInfo(long id);
}
