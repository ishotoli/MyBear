package listByteBufferPool;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author zhanyingda 2017/08/12
 *
 */
public class ByteBufferPool {

	private final MappedByteBuffer largeBuffer;
	//回收ByteBuffer的List
	private List<ByteBuffer> buffList;
	//要节分的ByteBufer大小
	private final int largeBufferSize;
	//每个分片的大小
	private final int sliceBufferSize;
	//用户连接数
	private final int connectCount;
	
	public ByteBufferPool(int largeBufferSize,int connectCount) throws IOException{
		RandomAccessFile memoryMappedFile = new RandomAccessFile("d:/largeBufferPool.txt","rw");
		largeBuffer =  memoryMappedFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, largeBufferSize);
		memoryMappedFile.close();
		buffList = new LinkedList<ByteBuffer>();
		this.largeBufferSize = largeBufferSize;
		this.connectCount = connectCount;
		this.sliceBufferSize = largeBufferSize/connectCount;
	}
	
	/**
	 * 分配ByteBuffer
	 * @return
	 */
	public synchronized ByteBuffer allocateBuffer(){
		ByteBuffer buffer;
		if(!buffList.isEmpty()){
			buffer = buffList.get(0);
			buffer.clear();
			buffList.remove(0);
		}
		else{
			buffer = getSliceBuffer();
		}
		
		System.out.println("分配了 " + (buffer == null ? 0 : buffer.capacity()) + "的空间");
		return buffer;
			
	}
	
	/**
	 * 分片
	 * @return
	 */
	private ByteBuffer getSliceBuffer(){
		if(largeBuffer.position() + sliceBufferSize > largeBuffer.capacity()){
			System.out.println("没有更多的可分配空间了");
			return null;
		}
		
		largeBuffer.limit(largeBuffer.position() + sliceBufferSize);
		ByteBuffer buffer = largeBuffer.slice();
		
		largeBuffer.position(largeBuffer.limit());
		largeBuffer.limit(largeBuffer.capacity());
		return buffer;
	}
	
	/**
	 * 回收ByteBuffer
	 * @param buffer
	 */
	public void recycleBuffer(ByteBuffer buffer){
		buffList.add(buffer);
		System.out.println("回收了 " + buffer.capacity() + "的空间");
	}
	

}
