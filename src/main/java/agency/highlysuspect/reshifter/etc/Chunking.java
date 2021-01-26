package agency.highlysuspect.reshifter.etc;

import net.minecraft.network.PacketByteBuf;

public class Chunking {
	public static final int CHUNK_SIZE = 1 << 19; //half a megabyte
	//public static final int CHUNK_SIZE = 1 << 10; //tiny amount to test that chunking works properly lol
	
	public static class Chunker {
		public Chunker(byte[] data) {
			this.data = data;
		}
		
		private final byte[] data;
		private int cursor = 0;
		
		public PacketByteBuf writeNextChunk(PacketByteBuf buf) {
			if(cursor == 0) buf.writeInt(data.length);
			if(cursor >= data.length) throw new IllegalStateException("Already wrote the entire output stream");
			
			if(cursor + CHUNK_SIZE < data.length) {
				buf.writeInt(CHUNK_SIZE);
				buf.writeBytes(data, cursor, CHUNK_SIZE);
			} else {
				buf.writeInt(data.length - cursor);
				buf.writeBytes(data, cursor, data.length - cursor);
			}
			
			cursor += CHUNK_SIZE;
			
			return buf;
		}
		
		public boolean isDone() {
			return cursor >= data.length;
		}
		
		public int totalLength() {
			return data.length;
		}
	}
	
	public static class Unchunker {
		private byte[] data;
		private int cursor;
		private boolean finished;
		
		public void handleByteBuf(PacketByteBuf buf) {
			if(finished) throw new IllegalStateException("Already processed all chunks");
			
			if(data == null) {
				data = new byte[buf.readInt()];
			}
			
			int length = buf.readInt();
			buf.readBytes(data, cursor, length);
			cursor += CHUNK_SIZE;
			
			if(cursor >= data.length) {
				finished = true;
			}
		}
		
		public boolean isFinished() {
			return finished;
		}
		
		public byte[] getBytes() {
			if(!finished) throw new IllegalStateException("Haven't processed all chunks yet");
			return data;
		}
	}
}
