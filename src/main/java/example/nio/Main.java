package example.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = 6789;
        try(ServerSocketChannel ch = ServerSocketChannel.open()){
            Selector selector = Selector.open();
            ch.bind(new InetSocketAddress(port));
            ch.configureBlocking(false);        //по молчанию режим Блокирующий
            SelectionKey key = ch.register(selector, SelectionKey.OP_ACCEPT);

            while(true){
                int numReady = selector.select();
                if(numReady==0) continue;

                Set<SelectionKey> keySet = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator= keySet.iterator();
                while (keyIterator.hasNext()){
                    SelectionKey k =keyIterator.next();
                    if(k.isAcceptable()){
                        ServerSocketChannel serv = (ServerSocketChannel) k.channel();
                        SocketChannel client = serv.accept();
                        if(client == null) continue;
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                        System.out.println("Добавлено соединение "+k);
                    }
                    if(k.isReadable()){

                        SocketChannel client = (SocketChannel) k.channel();
                        ByteBuffer requestBuf = ByteBuffer.allocate(100);
                        int r = client.read(requestBuf);
                        System.out.println("получено байт: "+r);
                        if(r == -1) client.close();
                        else{
                            byte[] result = new byte[r];
                            requestBuf.get(0, result);
                            System.out.println(" Получено: " + new String(result));
                            System.out.println("Arrays.toString(result) = " + Arrays.toString(result));
                            System.out.println(Thread.currentThread().getName());
                            String response = "Привет "+client.socket().getLocalAddress();
                            client.write(ByteBuffer.wrap(response.getBytes()));
                        }
                    }
                    keyIterator.remove();
                }
            }
        }
    }
}