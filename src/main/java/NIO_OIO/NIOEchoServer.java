package NIO_OIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NIOEchoServer {

    public void server(int port) throws IOException {

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        ServerSocket serverSocket = serverSocketChannel.socket();
        SocketAddress socketAddress = new InetSocketAddress(port);
        serverSocket.bind(socketAddress);
        //set no blocking
        serverSocketChannel.configureBlocking(false);

        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            try {
                selector.select();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            Set<SelectionKey> Readkeys = selector.selectedKeys();
            Iterator<SelectionKey> it = Readkeys.iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();
                try {
                    if (key.isAcceptable()) {

                        ServerSocketChannel serverSocketChannel1 = (ServerSocketChannel) key.channel();
                        SocketChannel socketChannel = serverSocketChannel1.accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, ByteBuffer.allocate(1000));
                    }
                    if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer bf = (ByteBuffer) key.attachment();
                        client.read(bf);
                    }
                    if (key.isWritable()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        ByteBuffer bf = (ByteBuffer) key.attachment();
                        socketChannel.write(bf);
                        bf.flip();
                        bf.compact();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    key.cancel();
                    key.channel().close();
                }
            }
        }


    }
}
