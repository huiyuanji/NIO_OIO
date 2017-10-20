package NIO_OIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

public class NIO2EchoServer {
    public void server(int port) throws IOException {

        final AsynchronousServerSocketChannel serverSocketChannel =
                AsynchronousServerSocketChannel.open();
        SocketAddress address = new InetSocketAddress(port);
        serverSocketChannel.bind(address);

        final CountDownLatch latch = new CountDownLatch(1);
        serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
            @Override
            public void completed(AsynchronousSocketChannel channel, Object attachment) {

                serverSocketChannel.accept(null,this);
                ByteBuffer byteBuffer = ByteBuffer.allocate(1000);
                channel.read(byteBuffer,byteBuffer,new EchoCompletionHandler(channel));
            }
            @Override
            public void failed(Throwable exc, Object attachment) {
                try {
                    serverSocketChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    latch.countDown();
                }
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            Thread.currentThread().interrupt();
        }
    }
    private class EchoCompletionHandler implements CompletionHandler<Integer,ByteBuffer> {
        private final AsynchronousSocketChannel channel;

        public EchoCompletionHandler(AsynchronousSocketChannel channel) {
            this.channel = channel;
        }
        @Override
        public void completed(Integer result, ByteBuffer attachment) {
            channel.write(attachment, attachment, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    if (attachment.hasRemaining()){
                        channel.write(attachment,attachment,this);
                    }else {
                        attachment.compact();
                        channel.read(attachment,attachment, EchoCompletionHandler.this);
//                        channel.read(attachment,attachment, EchoCompletionHandler.this);
                    }
                }
                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    try {
                        channel.close();
                        channel.read(attachment);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {

    }
}
