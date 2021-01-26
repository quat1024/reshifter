package agency.highlysuspect.reshifter.etc;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.minecraft.util.collection.IdList;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public interface IdListExt<T> {
	int cachedHash();
	
	static <T> int cachedHash(IdList<T> idList) {
		//noinspection unchecked
		return ((IdListExt<T>) idList).cachedHash();
	}
	
	default byte[] toByteArray(SpicyBiConsumer<T, DataOutput> writer) throws IOException {
		try (
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
		) {
			DataOutput out = new DataOutputStream(baos);
			
			//noinspection unchecked
			IdList<T> self = (IdList<T>) this;
			
			out.writeInt(self.size());
			for(T t : self) {
				writer.accept(t, out);
			}
			
			return baos.toByteArray();
		}
	}
	
	default byte[] toCompressedByteArray(SpicyBiConsumer<T, DataOutput> writer) throws IOException {
		try (
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			GZIPOutputStream gzWriter = new GZIPOutputStream(out);
		) {
			gzWriter.write(toByteArray(writer));
			gzWriter.finish();
			gzWriter.flush();
			return out.toByteArray();
		}
	}
	
	static <T> byte[] toCompressedByteArray(IdList<T> idList, SpicyBiConsumer<T, DataOutput> writer) throws IOException {
		//noinspection unchecked
		return ((IdListExt<T>) idList).toCompressedByteArray(writer);
	}
	
	static <T> IdList<T> fromByteArray(byte[] data, SpicyFunction<DataInput, T> reader) throws IOException {
		try (
			ByteArrayInputStream bais = new ByteArrayInputStream(data)
		) {
			DataInput in = new DataInputStream(bais);
			
			int size = in.readInt();
			IdList<T> list = new IdList<>(size);
			
			for(int i = 0; i < size; i++) list.add(reader.apply(in));
			
			return list;
		}
	}
	
	static <T> IdList<T> fromCompressedByteArray(byte[] data, SpicyFunction<DataInput, T> reader) throws IOException {
		try (
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			GZIPInputStream gzReader = new GZIPInputStream(bais);
		) {
			return fromByteArray(IOUtils.toByteArray(gzReader), reader);
		}
	}
	
	interface SpicyBiConsumer<A, B> {
		void accept(A a, B b) throws IOException;
	}
	
	interface SpicyFunction<A, B> {
		B apply(A a) throws IOException;
	}
}
