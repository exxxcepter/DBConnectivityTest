import java.sql.*;
import java.util.ArrayList;

public class ConnectivityTest {
    public static void main(String args[]){
        final int clientsQuantity = 250;
        final int requestQuantity = 1500;
        final int sleepTimeout = 200;
        final String dbURL = "jdbc:postgresql://localhost:5432/postgres";
        ArrayList<Connection> connections = new ArrayList<Connection>(clientsQuantity);

        //Блок создания подключений пользователей к БД
        System.out.println("Создание подключений...");
        for (int i = 1; i <= clientsQuantity; i++){
            try{
                    connections.add(DriverManager.getConnection(dbURL,"user" + i,"pass" + i));
            }
            catch (SQLException e){};
        }
        System.out.println(" > Подключено!");

        System.out.println("Очистка TestTable...");
        try{
            Statement statement = connections.get(0).createStatement();
            statement.executeUpdate("DELETE FROM TestTable");
        }
        catch (SQLException e) {};
        System.out.println(" > Очищено!");

        //Блок создания пользователей БД
        /*System.out.println("Создание пользователей...");
        try{
            Statement statement = DriverManager.getConnection(dbURL,"postgres","dbpass").createStatement();
            for (int id = 250; id < 251; id++){
                statement.executeUpdate("CREATE USER user" + id + " WITH\n" +
                        "\tLOGIN\n" +
                        "\tNOSUPERUSER\n" +
                        "\tNOCREATEDB\n" +
                        "\tNOCREATEROLE\n" +
                        "\tINHERIT\n" +
                        "\tNOREPLICATION\n" +
                        "\tCONNECTION LIMIT -1\n" +
                        "\tPASSWORD 'pass" + id + "';\n" +
                        "\n" +
                        "GRANT postgres TO user" + id + ";");
            }
        }
        catch (SQLException e) {};
        System.out.println(" > Пользователи созданы!");*/

        //Блок запуска потоков-подключений для выполнения операции INSERT
        System.out.println("Выполнение операций INSERT...");
        final long startTime = System.currentTimeMillis();
        for (int i = 1; i <= clientsQuantity; i++){
            final ArrayList<Connection> newConnections = connections;
            final int index = i;
            new Thread(){
                public void run(){
                    String userID = "user" + index;
                    try{
                        Statement statement = newConnections.get(index - 1).createStatement();
                        for (int c = 0; c < requestQuantity; c++){
                            statement.executeUpdate("INSERT INTO TestTable (test1, test2) VALUES ('" + userID + "', " + sleepTimeout + ")");
                            Thread.sleep(sleepTimeout);
                        }
                    }
                    catch (Exception e) {};
                }
            }.start();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                final long endTime = System.currentTimeMillis();
                final long runTime = endTime - startTime;
                final float floatRunTime = (float)runTime/(1000 * 60);
                System.out.println(" > Операции выполнены!\n");
                System.out.println("Число пользователей: " + clientsQuantity);
                System.out.println("Число заявок от одного пользователя: " + requestQuantity);
                System.out.println("Частота заявок от одного пользователя: " + sleepTimeout + "мс");
                System.out.print("Затрачено ");
                System.out.printf("%.2f", floatRunTime);
                System.out.print("мин\n");
            }
        }));
    }
}
