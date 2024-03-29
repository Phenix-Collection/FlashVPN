package com.polestar.task.database;

import android.content.Context;
import android.content.res.AssetManager;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.polestar.ad.AdLog;
import com.polestar.task.network.datamodels.Product;
import com.polestar.task.network.datamodels.Task;
import com.polestar.task.network.datamodels.User;
import com.polestar.task.network.responses.ProductsResponse;
import com.polestar.task.network.responses.TasksResponse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class DatabaseFileImpl implements DatabaseApi {
    ArrayList<Task> mTasks = new ArrayList<>();
    ArrayList<Product> mProducts = new ArrayList<>();
    User mUser;

    private Context mContext;
    protected static DatabaseFileImpl sInstance = null;
    private Gson mGson = new Gson();

    private static final String DIR = "ad";
    private static final String TASK_FILE = DIR + "/tasks.txt";
    private static final String PRODUCT_FILE = DIR + "/products.txt";
    private static final String USER_FILE = DIR + "/user.txt";

    private boolean needTask;
    private boolean needProduct;

    @Override
    public boolean isDataAvailable() {
        return mUser != null && (needProduct? mProducts!= null && mProducts.size() > 0 : true)
                && (needTask? mTasks != null && mTasks.size() > 0: true);
    }

    public synchronized static DatabaseApi getDatabaseFileImpl(Context context, boolean needTask, boolean needProduct) {
        if (sInstance == null) {
            sInstance = new DatabaseFileImpl(context, needTask, needProduct);
        }
        return sInstance;
    }

    protected DatabaseFileImpl(Context context, boolean needTask, boolean needProduct) {
        mContext = context;
        this.needTask = needTask;
        this.needProduct = needProduct;
        loadUserInfo(USER_FILE);
        if (needTask) {
            loadTasks(TASK_FILE);
        }
        if (needProduct) {
            loadProducts(PRODUCT_FILE);
        }
    }

    private void createDirIfNotExist(String dirName) {
        File file = new File(mContext.getFilesDir(), dirName);
        if (!file.exists()) {
            file.mkdirs();
        }
    }


    private void loadTasks(String fileName) {
        createDirIfNotExist(DIR);
        File file = new File(mContext.getFilesDir(), fileName);

        synchronized (TASK_FILE) {
            String taskInfo = readOnelineFromFile(file.getAbsolutePath());
            if (taskInfo == null) {
                mTasks = null;
            } else {
                try {
                    TasksResponse tasksResponse = mGson.fromJson(taskInfo, TasksResponse.class);
                    if (tasksResponse != null) {
                        mTasks = tasksResponse.mTasks;
                    }
                } catch (JsonSyntaxException jse) {
                    mTasks = null;
                }
            }
        }
//        if (mTasks == null ) {
//            String json = getJson("tasks", mContext);
//            TasksResponse tasksResponse = mGson.fromJson(json, TasksResponse.class);
//            if (tasksResponse != null) {
//                mTasks = tasksResponse.mTasks;
//            }
//        }
    }

    // called with lock held outside
    private boolean storeTasksSynced(ArrayList<Task> tasks, String fileName) {
        TasksResponse tasksResponse = new TasksResponse();
        tasksResponse.mTasks = tasks;
        File file = new File(mContext.getFilesDir(), fileName);
        return writeOneLineToFile(file.getAbsolutePath(), mGson.toJson(tasksResponse));
    }

    private void loadProducts(String fileName) {
        createDirIfNotExist(DIR);
        File file = new File(mContext.getFilesDir(), fileName);

        synchronized (PRODUCT_FILE) {
            String productInfo = readOnelineFromFile(file.getAbsolutePath());
            if (productInfo == null) {
                mProducts = null;
            } else {
                try {
                    ProductsResponse productsResponse = mGson.fromJson(productInfo, ProductsResponse.class);
                    if (productsResponse != null) {
                        mProducts = productsResponse.mProducts;
                    }else{
                        //wrong PRODUCT_FILE, check default from assests
                    }
                } catch (JsonSyntaxException jse) {
                    mProducts = null;
                }
            }
        }
//        if (mProducts == null ) {
//            String json = getJson("products", mContext);
//            ProductsResponse productsResponse = mGson.fromJson(json, ProductsResponse.class);
//            if (productsResponse != null) {
//                mProducts = productsResponse.mProducts;
//            }
//        }
        if (mProducts != null) {
            AdLog.i(TAG, "Loaded " + mProducts.size() + " products from disk");
        } else {
            AdLog.i(TAG, "Loaded 0 products from disk");
        }
    }

    // called with lock held outside
    private boolean storeProductsSynced(ArrayList<Product> products, String fileName) {
        ProductsResponse productsResponse = new ProductsResponse();
        productsResponse.mProducts = products;
        File file = new File(mContext.getFilesDir(), fileName);
        return writeOneLineToFile(file.getAbsolutePath(), mGson.toJson(productsResponse));
    }

    private boolean storeUserSynced(User user, String fileName) {
        if (user != null) {
            File file = new File(mContext.getFilesDir(), fileName);
            return writeOneLineToFile(file.getAbsolutePath(), mGson.toJson(mUser));
        } else {
            AdLog.i(TAG, "Invalid user info");
            return false;
        }
    }

    private void loadUserInfo(String fileName) {
        createDirIfNotExist(DIR);
        File file = new File(mContext.getFilesDir(), fileName);

        synchronized (USER_FILE) {
            String userInfo = readOnelineFromFile(file.getAbsolutePath());
            if (userInfo == null) {
                mUser = null;
            } else {
                try {
                    mUser = mGson.fromJson(userInfo, User.class);
                } catch (JsonSyntaxException jse) {
                    mUser = null;
                }
            }
//            if (mUser == null ) {
//                String json = getJson("user", mContext);
//                mUser= mGson.fromJson(json, User.class);
//            }
            if (mUser != null) {
                AdLog.i(TAG, "Loaded user info " + mGson.toJson(mUser));
            } else {
                AdLog.e(TAG, "No user info loaded");
            }
        }
    }

    @Override
    public List<Task> getActiveTasks() {
        synchronized (TASK_FILE) {
            return mTasks;
        }
    }

    @Override
    public boolean setActiveTasks(ArrayList<Task> tasks) {
        synchronized (TASK_FILE) {
            mTasks = tasks;
            return storeTasksSynced(mTasks, TASK_FILE);
        }
    }

    @Override
    public List<Task> getActiveTasksByType(int type) {
        ArrayList<Task> ret = new ArrayList<>();
        synchronized (TASK_FILE) {
            if (mTasks != null) {
                for (Task task : mTasks) {
                    AdLog.d(TAG, task.toString());
                    if (task.mTaskType == type) {
                        ret.add(task);
                    }
                }
            }
        }
        return ret;
    }

    @Override
    public Task getTaskById(long taskId) {
        synchronized (TASK_FILE) {
            if (mTasks != null) {
                for (Task task : mTasks) {
                    if (task.mId == taskId) {
                        return task;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public User getMyUserInfo() {
        return mUser;
    }

    @Override
    public boolean setUserInfo(User user) {
        synchronized (USER_FILE) {
            mUser = user;
            return storeUserSynced(mUser, USER_FILE);
        }
    }

    @Override
    public List<Product> getAllProductInfo() {
        synchronized (PRODUCT_FILE) {
            return mProducts;
        }
    }

    @Override
    public boolean setActiveProducts(ArrayList<Product> products) {
        synchronized (PRODUCT_FILE) {
            mProducts = products;
            return storeProductsSynced(mProducts, PRODUCT_FILE);
        }
    }

//    @Override
//    public List<ProductInfoNoUse> getPurchasedProducts() {
//        return null;
//    }

    @Override
    public Product getProductInfo(long id) {
        synchronized (PRODUCT_FILE) {
            if (mProducts != null) {
                for (Product product : mProducts) {
                    if (product.mId == id) {
                        return product;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public List<Product> getProductInfoByType(int type) {
        ArrayList<Product> ret = new ArrayList<>();
        synchronized (PRODUCT_FILE) {
            if (mProducts != null) {
                for (Product product : mProducts) {
                    if (product.mProductType == type) {
                        ret.add(product);
                    }
                }
            }
        }
        return ret;
    }

    public static String readOnelineFromFile(String fileName) {
        InputStream instream = null;
        String line = null;
        try {
// open the file for reading
            instream = new FileInputStream(fileName);

// if file the available for reading
            if (instream != null) {
                // prepare the file for reading
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);

                line = buffreader.readLine();
                buffreader.close();
                return line;
            }
        } catch (Exception ex) {
            // print stack trace.
            return null;
        } finally {
// close the file.
            if (instream != null) {
                try {
                    instream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return line;
        }
    }

    public static boolean writeOneLineToFile(String filePath, String info) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        File newFile = new File(filePath);
        try {
            newFile.createNewFile();
            newFile.setReadable(true, false);
            newFile.setWritable(true, false);
        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }

        if (info != null && !info.isEmpty()) {
            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)));
                out.println(info);
                out.close();
            } catch (IOException e) {
                //exception handling left as an exercise for the reader
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return file.delete();
        }
        return true;
    }
}
