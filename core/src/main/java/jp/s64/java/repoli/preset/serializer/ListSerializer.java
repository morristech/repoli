/*
 * Copyright (C) 2017 Shuma Yoshioka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.s64.java.repoli.preset.serializer;

import com.google.common.reflect.TypeToken;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import jp.s64.java.repoli.base.SerializerUserHelper;
import jp.s64.java.repoli.core.ISerializer;

public class ListSerializer implements ISerializer {

    public static ListSerializer INSTANCE = newInstance();

    public static ListSerializer newInstance() {
        return new ListSerializer();
    }

    protected ListSerializer() {

    }

    @Override
    public <T> T deserialize(TypeToken<T> type, byte[] serialized, Set<ISerializer> serializers) {
        SerializerUserHelper helper = new SerializerUserHelper();
        {
            helper.addSerializer(serializers);
        }
        TypeToken<?> innerType = type.resolveType(type.getRawType().getTypeParameters()[0]);

        ByteArrayInputStream in = new ByteArrayInputStream(serialized);
        ObjectInputStream ois = null;

        List<byte[]> org = null;
        try {
            ois = new ObjectInputStream(in);
            org = (List<byte[]>) ois.readObject();
        } catch (IOException e) {
            throw new ListSerializerException(e);
        } catch (ClassNotFoundException e) {
            throw new ListSerializerException(e);
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
                in.close();
            } catch (IOException e) {
                throw new ListSerializerException(e);
            }
        }

        List<Object> dest = null;

        if (org != null) {
            dest = new ArrayList<>();
            for (byte[] item : org) {
                Object deserialized = helper.deserializeByClass(innerType, item);
                dest.add(deserialized);
            }
        }

        return (T) dest;
    }

    @Override
    public <T> byte[] serialize(TypeToken<T> type, Object deserialized, Set<ISerializer> serializers) {
        SerializerUserHelper helper = new SerializerUserHelper();
        {
            helper.addSerializer(serializers);
        }
        List<byte[]> dest = new LinkedList<>();

        List<?> list = (List<?>) deserialized;
        TypeToken<?> innerType = type.resolveType(type.getRawType().getTypeParameters()[0]);

        for (Object item : list) {
            byte[] serialized = helper.serializeByClass(innerType, item);
            dest.add(serialized);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(out);
            {
                oos.writeObject(dest);
                oos.flush();
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new ListSerializerException(e);
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
                out.close();
            } catch (IOException e) {
                throw new ListSerializerException(e);
            }
        }
    }

    @Override
    public boolean canSerialize(TypeToken<?> type) {
        return List.class.isAssignableFrom(type.getRawType());
    }

    @Override
    public float getPriority() {
        return 1;
    }

    public static class ListSerializerException extends RuntimeException {

        public ListSerializerException(Throwable throwable) {
            super(throwable);
        }

    }

}
