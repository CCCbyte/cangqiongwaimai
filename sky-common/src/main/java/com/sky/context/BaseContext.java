package com.sky.context;

public class BaseContext {

    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 设置当前线程的ID值
     * @param id 要设置的ID值，类型为Long
     */
    public static void setCurrentId(Long id) {
        // 使用threadLocal变量设置当前线程的ID值
        threadLocal.set(id);
    }

    /**
     * 获取当前线程的ID值
     * 通过ThreadLocal机制获取当前线程关联的ID
     *
     * @return 返回当前线程的ID值，类型为Long
     */
    public static Long getCurrentId() {
    // 从ThreadLocal中获取当前线程关联的ID值并返回
        return threadLocal.get();
    }

    /**
     * 移除当前线程的ThreadLocal变量值
     * 该方法用于清理线程局部变量，防止内存泄漏
     */
    public static void removeCurrentId() {
        threadLocal.remove(); // 调用ThreadLocal的remove方法清除当前线程的值
    }

}
