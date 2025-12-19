#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
测试认证功能
"""
import sys
import io

# 设置标准输出编码为UTF-8
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
    sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')

import sqlite3
import hashlib
from server import DatabaseManager

def test_authentication():
    print("🔍 测试认证功能...")
    
    # 初始化数据库
    db = DatabaseManager()
    
    # 检查管理员账户
    conn = sqlite3.connect('elderly_assistant.db')
    cursor = conn.cursor()
    
    cursor.execute('SELECT COUNT(*) FROM admin_users')
    admin_count = cursor.fetchone()[0]
    print(f"📊 管理员账户数量: {admin_count}")
    
    if admin_count > 0:
        cursor.execute('SELECT id, username, password FROM admin_users')
        admins = cursor.fetchall()
        for admin in admins:
            print(f"  - ID: {admin[0]}, 用户名: {admin[1]}, 密码哈希: {admin[2][:20]}...")
            
            # 测试密码验证
            test_password = 'admin123'
            hashed_password = hashlib.sha256(test_password.encode()).hexdigest()
            print(f"  - 测试密码 '{test_password}' 的哈希: {hashed_password[:20]}...")
            print(f"  - 密码匹配: {admin[2] == hashed_password}")
    
    # 测试认证方法
    print("\n🧪 测试认证方法...")
    user = db.authenticate_user('admin', 'admin123')
    if user:
        print(f"✅ 认证成功: {user}")
    else:
        print("❌ 认证失败")
    
    conn.close()

if __name__ == '__main__':
    test_authentication()