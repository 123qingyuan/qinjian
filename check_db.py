#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
检查数据库状态脚本
"""
import sqlite3

def check_database():
    conn = sqlite3.connect('elderly_assistant.db')
    cursor = conn.cursor()
    
    # 检查表
    cursor.execute("SELECT name FROM sqlite_master WHERE type='table'")
    tables = cursor.fetchall()
    print('数据库表:', [table[0] for table in tables])
    
    # 检查用户数据
    cursor.execute('SELECT COUNT(*) FROM users')
    user_count = cursor.fetchone()[0]
    print(f'用户数量: {user_count}')
    
    # 检查使用日志
    cursor.execute('SELECT COUNT(*) FROM usage_logs')
    log_count = cursor.fetchone()[0]
    print(f'使用日志数量: {log_count}')
    
    # 检查异常事件
    cursor.execute('SELECT COUNT(*) FROM abnormal_events')
    event_count = cursor.fetchone()[0]
    print(f'异常事件数量: {event_count}')
    
    # 显示用户详情
    cursor.execute('SELECT id, name, phone, device_id, last_active FROM users LIMIT 5')
    users = cursor.fetchall()
    print('\n用户详情:')
    for user in users:
        print(f'  ID: {user[0]}, 姓名: {user[1]}, 电话: {user[2]}, 设备: {user[3]}, 最后活跃: {user[4]}')
    
    conn.close()

if __name__ == '__main__':
    check_database()