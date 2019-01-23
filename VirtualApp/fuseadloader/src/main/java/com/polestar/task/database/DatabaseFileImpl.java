package com.polestar.task.database;

import android.content.Context;

import com.google.gson.Gson;
import com.polestar.task.IProductStatusListener;
import com.polestar.task.ITaskStatusListener;
import com.polestar.task.database.datamodels.ProductInfoNoUse;
import com.polestar.task.network.datamodels.Product;
import com.polestar.task.network.datamodels.Task;
import com.polestar.task.network.datamodels.User;
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
    public static final String TAG = "Database";

    ArrayList<Task> mTasks = new ArrayList<>();
    ArrayList<Product> mProducts = new ArrayList<>();
    User mUser;

    private Context mContext;
    private static DatabaseFileImpl sInstance = null;
    private Gson mGson = new Gson();

    private static final String DIR = "ad";
    private static final String TASK_FILE = DIR + "/tasks.txt";
    private static final String PRODUCT_FILE = DIR + "/products.txt";
    private static final String USER_FILE = DIR + "/user.txt";


    public static DatabaseApi getDatabaseFileImpl(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseFileImpl(context);
        }
        return sInstance;
    }

    private DatabaseFileImpl(Context context) {
        mContext = context;

        loadTasks(TASK_FILE);
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
                TasksResponse tasksResponse = mGson.fromJson(taskInfo, TasksResponse.class);
                if (tasksResponse != null) {
                    mTasks = tasksResponse.mTasks;
                }
            }
        }
    }

    // called with lock held outside
    private boolean storeTasksSynced(ArrayList<Task> tasks, String fileName) {
        TasksResponse tasksResponse = new TasksResponse();
        tasksResponse.mTasks = tasks;
        File file = new File(mContext.getFilesDir(), fileName);
        return writeOneLineToFile(file.getAbsolutePath(), mGson.toJson(tasksResponse));
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
        return false;
    }

    @Override
    public List<Product> getAllProductInfo() {
        return null;
    }

    @Override
    public boolean setActiveProducts(ArrayList<Product> products) {
        return false;
    }

//    @Override
//    public List<ProductInfoNoUse> getPurchasedProducts() {
//        return null;
//    }

    @Override
    public Product getProductInfo(long id) {
        return null;
    }

    @Override
    public void consumeProduct(String deviceId, long productId, int amount, IProductStatusListener listener) {

    }

    @Override
    public void requestFinishTask(String deviceId, long taskId, ITaskStatusListener listener) {

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
