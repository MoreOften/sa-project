package ku.cs.services;

import java.util.HashMap;
import java.util.Map;

/**
 * คลาสสำหรับส่งข้อมูลข้าม Controller (Scene)
 * ใช้งานโดยการ "ฝาก" ข้อมูลไว้ใน Map (ด้วย key)
 * แล้ว Controller ปลายทางก็ "ถอน" ข้อมูลโดยใช้ key เดียวกัน
 *
 * คลาสนี้ใช้ static Map เพื่อให้ข้อมูลคงอยู่ตลอดการทำงานของแอปพลิเคชัน
 */
public class DataBox {

    // ใช้ Map แบบ static เพื่อให้ข้อมูลคงอยู่ตลอดการทำงาน
    private static Map<String, Object> data = new HashMap<>();

    /**
     * เมธอด static สำหรับฝากข้อมูล
     * @param key สตริงสำหรับเป็นกุญแจ
     * @param value อ็อบเจกต์ที่ต้องการส่ง
     */
    public static void put(String key, Object value) {
        data.put(key, value);
    }

    /**
     * เมธอด static สำหรับถอนข้อมูล
     * @param key สตริงกุญแจที่ตรงกับตอนฝาก
     * @return อ็อบเจกต์ที่ถูกฝากไว้ (ต้อง cast กลับเป็น Type เดิม)
     */
    public static Object get(String key) {
        return data.get(key);
    }

    /**
     * (แนะนำ) เมธอดสำหรับถอนข้อมูลและลบออกทันที
     * ป้องกันไม่ให้ข้อมูลค้างอยู่ในหน่วยความจำโดยไม่จำเป็น
     * @param key สตริงกุญแจ
     * @return อ็อบเจกต์ที่ถูกฝากไว้
     */
    public static Object getAndRemove(String key) {
        return data.remove(key);
    }

    /**
     * (เผื่อใช้) เมธอดสำหรับล้างข้อมูลทั้งหมดใน DataBox
     */
    public static void clear() {
        data.clear();
    }
}