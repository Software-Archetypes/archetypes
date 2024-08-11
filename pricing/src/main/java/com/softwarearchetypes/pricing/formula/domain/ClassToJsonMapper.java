package com.softwarearchetypes.pricing.formula.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import java.lang.reflect.Field;

class ClassToJsonMapper {

    static String mapClassToJson(final Class<?> clazz) throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var json = createJsonStructure(clazz, mapper);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
    }


    private static ObjectNode createJsonStructure(Class<?> clazz, ObjectMapper objectMapper) {
        ObjectNode jsonNode = objectMapper.createObjectNode();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            Class<?> fieldType = field.getType();
            if (fieldType.isPrimitive() || fieldType == String.class || Number.class.isAssignableFrom(fieldType) || Boolean.class.isAssignableFrom(fieldType)) {
                jsonNode.set(field.getName(), getDefaultValueNode(fieldType));
            } else {
                jsonNode.set(field.getName(), createJsonStructure(fieldType, objectMapper));
            }
        }

        return jsonNode;
    }

    private static ValueNode getDefaultValueNode(Class<?> fieldType) {
        if (fieldType == boolean.class || fieldType == Boolean.class) {
            return JsonNodeFactory.instance.booleanNode(false);
        } else if (fieldType == char.class || fieldType == Character.class) {
            return JsonNodeFactory.instance.textNode("");
        } else if (fieldType == byte.class || fieldType == short.class || fieldType == int.class || fieldType == long.class || Number.class.isAssignableFrom(fieldType)) {
            return JsonNodeFactory.instance.numberNode(1);
        } else if (fieldType == float.class || fieldType == double.class) {
            return JsonNodeFactory.instance.numberNode(1.0);
        } else if (fieldType == String.class) {
            return JsonNodeFactory.instance.textNode("");
        }
        return JsonNodeFactory.instance.nullNode();
    }

}
