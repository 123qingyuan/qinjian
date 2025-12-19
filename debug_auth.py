#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
调试认证功能
"""
import sqlite3
import hashlib
import json
from http.server import HTTPServer, BaseHTTPRequestHandler
from urllib.parse import urlparse, parse_qs

# 数据库文件
DB_FILE = 'elderly_assistant.db'

def init_database():
    """初始化数据库"""
    conn = sqlite3.connect(DB_FILE)
    cursor = conn.cursor()
    
    # 创建管理员表
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS admin_users (
            id TEXT PRIMARY KEY,
            username TEXT UNIQUE NOT NULL,
            password TEXT NOT NULL,
            email TEXT,
            role TEXT DEFAULT 'admin'
        )
    ''')
    
    conn.commit()
    conn.close()

def create_admin_user():
    """创建管理员用户"""
    conn = sqlite3.connect(DB_FILE)
    cursor = conn.cursor()
    
    # 清空现有用户
    cursor.execute('DELETE FROM admin_users')
    
    # 创建新管理员
    password = 'admin123'
    hashed_password = hashlib.sha256(password.encode()).hexdigest()
    
    cursor.execute('''
        INSERT INTO admin_users (id, username, password, email, role)
        VALUES (?, ?, ?, ?, ?)
    ''', ('admin', 'admin', hashed_password, 'admin@elderly.com', 'super_admin'))
    
    conn.commit()
    conn.close()
    
    print(f'管理员用户创建成功')
    print(f'用户名: admin')
    print(f'密码: admin123')
    print(f'密码哈希: {hashed_password}')

def verify_auth(username, password):
    """验证认证"""
    conn = sqlite3.connect(DB_FILE)
    cursor = conn.cursor()
    
    # 计算密码哈希
    hashed_password = hashlib.sha256(password.encode()).hexdigest()
    
    print(f'尝试认证用户: {username}')
    print(f'输入密码哈希: {hashed_password}')
    
    # 查询用户
    cursor.execute('''
        SELECT id, username, email, role FROM admin_users 
        WHERE username = ? AND password = ?
    ''', (username, hashed_password))
    
    user = cursor.fetchone()
    conn.close()
    
    if user:
        print(f'认证成功: {user}')
        return {
            'id': user[0],
            'username': user[1],
            'email': user[2],
            'role': user[3]
        }
    else:
        print('认证失败: 用户不存在或密码错误')
        return None

class AuthHandler(BaseHTTPRequestHandler):
    def do_POST(self):
        if self.path == '/api/auth/login':
            self.handle_login()
    
    def handle_login(self):
        try:
            content_length = int(self.headers['Content-Length'])
            post_data = self.rfile.read(content_length)
            data = json.loads(post_data.decode('utf-8'))
            
            username = data.get('username')
            password = data.get('password')
            
            print(f'收到登录请求: 用户名={username}, 密码长度={len(password) if password else 0}')
            
            if not username or not password:
                self.send_error_response('用户名和密码不能为空')
                return
            
            user = verify_auth(username, password)
            
            if user:
                response = {
                    'success': True,
                    'user': user,
                    'message': '登录成功'
                }
                self.send_json_response(response)
            else:
                self.send_error_response('用户名或密码错误')
                
        except Exception as e:
            print(f'登录处理错误: {e}')
            self.send_error_response('服务器错误')
    
    def send_json_response(self, data):
        json_data = json.dumps(data, ensure_ascii=False)
        self.send_response(200)
        self.send_header('Content-Type', 'application/json; charset=utf-8')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.end_headers()
        self.wfile.write(json_data.encode('utf-8'))
    
    def send_error_response(self, message):
        error_data = {'error': message}
        json_data = json.dumps(error_data, ensure_ascii=False)
        self.send_response(401)
        self.send_header('Content-Type', 'application/json; charset=utf-8')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.end_headers()
        self.wfile.write(json_data.encode('utf-8'))
    
    def log_message(self, format, *args):
        pass  # 禁用日志输出

def main():
    print('🔍 启动认证调试服务器...')
    
    # 初始化数据库
    init_database()
    create_admin_user()
    
    # 启动调试服务器
    server = HTTPServer(('localhost', 3001), AuthHandler)
    print('调试服务器启动在 http://localhost:3001')
    print('测试命令: curl -X POST http://localhost:3001/api/auth/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin123\"}"')
    
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print('\n服务器停止')

if __name__ == '__main__':
    main()