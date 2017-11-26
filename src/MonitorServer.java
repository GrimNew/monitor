//导入外部jar包
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Grim on 2017-02-17.
 */
public class MonitorServer {
    private static final String url=
            "jdbc:mysql://localhost:3306/monitor" +//JDBC方式/MySQL数据库/本机/端口3306/数据库名称
                    "?useSSL=false&useUnicode=true&characterEncoding=utf8";//SSL关闭/使用Unicode编码/编码方式utf-8
    private static final String user="root";
    private static final String password="root";

    public static void main(String[] args) {
        //创建线程池等待任务队列，数量10
        BlockingQueue<Runnable> blockingQueue=new ArrayBlockingQueue<>(10);
        //创建线程池，基本线程5个，最大20个，存活时间2分钟，源自于等待任务队列
        ThreadPoolExecutor threadPoolExecutor=new ThreadPoolExecutor(
                5,20,2, TimeUnit.MINUTES ,blockingQueue);

        //创建对象
        Connection connection =null;
        ServerSocket serverSocket=null;
        Socket socket=null;
        Statement statement=null;

        try {
            //反射加载驱动
            Class.forName("com.mysql.jdbc.Driver");
            //连接数据库对象实例化
            connection = DriverManager.getConnection(url,user,password);
            if (connection != null){
                System.out.println("数据库连接成功！");
            }
            //获取statement数据库操作实例
            if (connection != null) {
                statement= connection.createStatement();
            }
            //serverSocket实例绑定6000端口
            serverSocket = new ServerSocket(60000);

            while (statement!=null) {
                //启动侦听并进入阻塞状态
                socket = serverSocket.accept();
                //启动线程
                threadPoolExecutor.execute(new MonitorServerThread(statement, socket));
            }

        } catch (ClassNotFoundException | SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            //关闭相关资源
            try {
                if (socket != null) {
                    socket.close();
                }
                if (serverSocket != null) {
                    serverSocket.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
                threadPoolExecutor.shutdown();
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
