package com.example.android.bluetoothlegatt.mpu6050;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import com.example.android.bluetoothlegatt.util.*;;



/**
 * A vertex shaded cube.
 */
public class Cube
{
	public Cube()
	{
		int one = 0x10000;

		

		//立方�? 个顶�?
		int vertices[] = {
				-one, -one, -one,
				one, -one, -one,
				one,  one, -one,
				-one,  one, -one,
				-one, -one,  one,
				one, -one,  one,
				one,  one,  one,
				-one,  one,  one,
		};

		int colors[] = {
				0,    0,    0,  one,
				one,    0,    0,  one,
				one,  one,    0,  one,
				0,  one,    0,  one,
				0,    0,  one,  one,
				one,    0,  one,  one,
				one,  one,  one,  one,
				0,  one,  one,  one,
		};

		//顶点索引12个三角形(立方体一面由两个三角形组�?
		byte indices[] = {
				0, 4, 5,    0, 5, 1,
				1, 5, 6,    1, 6, 2,
				2, 6, 7,    2, 7, 3,
				3, 7, 4,    3, 4, 0,
				4, 7, 6,    4, 6, 5,
				3, 0, 1,    3, 1, 2
		};
		triggerBuffer = Arr2BufferUtils.getBuffer(new int[]{
				0, one, 0,
				-one, -one, 0,
				one, -one, 0
		});
		
		 quaterBuffer = Arr2BufferUtils.getBuffer(new int[]{
				one, -one, 0,
				one, one, 0,
				-one, one, 0,
				-one, -one, 0
				
		});
		// Buffers to be passed to gl*Pointer() functions
		// must be direct, i.e., they must be placed on the
		// native heap where the garbage collector cannot
		// move them.
		//
		// Buffers with multi-byte datatypes (e.g., short, int, float)
		// must have their byte order set to native order

		//int -> 4byte
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length*4);
		vbb.order(ByteOrder.nativeOrder()); //设置字节顺序 ->nativeOrder 本机字节顺序
		mVertexBuffer = vbb.asIntBuffer();
		mVertexBuffer.put(vertices);
		mVertexBuffer.position(0); //设置缓冲区起始位�?

		ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length*4);
		cbb.order(ByteOrder.nativeOrder());
		mColorBuffer = cbb.asIntBuffer();
		mColorBuffer.put(colors);
		mColorBuffer.position(0);

		mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
		mIndexBuffer.put(indices);
		mIndexBuffer.position(0);
	}

	public void draw(GL10 gl)
	{
		gl.glFrontFace(GL10.GL_CW);
		/**
		 * glVertexPointer(int size, int type, int stride, Buffer pointer)方法
		 * 设置顶点的位置数据�?这个方法中pointer参数用于指定顶点坐标值，
		 * 但这里并未使用三维数组来指定每个顶点X、Y、Z坐标值，point依然是一个一维数组，
		 * 也就是该数组将会包含3N个数值，每三个�?指定�?��顶点的X、Y、Z坐标值�? 
		 * 第一个参数size指定多少个元素指定一个顶点位置，该size参数通常总是3�?
		 * type参数指定顶点值的类型，如果顶点坐标�?为float类型，则指定为GL10.GL_FLOAT�?
		 * 如果顶点坐标值为整数，则指定为GL10.GL_FIXED.
		 * stride,//取数据时的间�?因为有些人习惯把颜色数据也放里边,如果这样,
		 * 你需要跳过这些点.意�?就是每隔stride取一个点
		 */
		gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer);	//设置顶点数组
		gl.glColorPointer(4, GL10.GL_FIXED, 0, mColorBuffer);	//设置颜色数组
		gl.glDrawElements(GL10.GL_TRIANGLES , 36, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);

	}


	private IntBuffer   mVertexBuffer;
	private IntBuffer   mColorBuffer;
	private ByteBuffer  mIndexBuffer;
	private IntBuffer 	triggerBuffer ;
	private IntBuffer 	quaterBuffer;
}