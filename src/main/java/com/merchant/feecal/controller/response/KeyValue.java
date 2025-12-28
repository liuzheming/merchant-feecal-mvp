package com.merchant.feecal.controller.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : kaerKing
 * @date : 2023/10/19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeyValue<K, V> {
    private K key;
    private V value;

    public static <K, V> KeyValue<K, V> of(K key, V value) {
        return new KeyValue<>(key, value);
    }
}
