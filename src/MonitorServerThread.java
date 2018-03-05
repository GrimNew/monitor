import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by Grim on 2017-02-19.
 */

//创建独立线程类，继承Runnable接口
public class MonitorServerThread implements Runnable{

    //创建对象
    private Statement statement;
    private Socket socket;

    //构造函数完成初始化类
    MonitorServerThread(Statement statement, Socket socket){
        this.statement = statement;
        this.socket=socket;
    }

    /*
    * 重写run()方法，完成线程功能
    * */
    @Override
    public void run(){

        String T_SQL;   //T-SQL语句字符串
        String[] splitMessage;  //存储拆分消息字符串数组
        String message; //消息字符串
        try (//创建输入流对象
             InputStream inputStream = socket.getInputStream();
             //读取输入流，注：一次读取一行，发送时必须要换行
             InputStreamReader inputStreamReader=new InputStreamReader(inputStream);
             //缓存输入流
             BufferedReader bufferedReader=new BufferedReader(inputStreamReader)){

            while (//循环读取行
                    (message=bufferedReader.readLine())!=null){
                //调用split()方法以“#”拆分若干段
                splitMessage =  message.split("#");
                //拼接T-SQL语句
                T_SQL="INSERT INTO data_table(device_ID,digital,analog) VALUES('"
                        +splitMessage[0]+"','"+splitMessage[1]+"','"+splitMessage[2]+"')";
                //执行T-SQL语句，执行成功返回不为NULL
                statement.executeUpdate(T_SQL);
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}
