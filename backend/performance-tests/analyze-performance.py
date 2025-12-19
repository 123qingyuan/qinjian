#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
老人监护管理系统性能分析脚本
作者: System
版本: 1.0.0
"""

import sys
import json
import csv
from datetime import datetime
from collections import defaultdict
import statistics

def analyze_performance(jtl_file, output_file):
    """分析JMeter测试结果并生成HTML报告"""
    
    # 读取JTL文件
    results = []
    try:
        with open(jtl_file, 'r', encoding='utf-8') as f:
            reader = csv.DictReader(f)
            for row in reader:
                results.append(row)
    except Exception as e:
        print(f"读取JTL文件失败: {e}")
        return False
    
    if not results:
        print("JTL文件为空或格式错误")
        return False
    
    # 分析数据
    analysis = analyze_results(results)
    
    # 生成HTML报告
    generate_html_report(analysis, output_file)
    
    return True

def analyze_results(results):
    """分析测试结果"""
    
    analysis = {
        'summary': {},
        'by_sampler': {},
        'response_times': {},
        'errors': {},
        'throughput': {},
        'recommendations': []
    }
    
    # 按采样器分组
    sampler_data = defaultdict(list)
    error_data = defaultdict(list)
    
    for result in results:
        sampler = result['label']
        timestamp = int(result['timeStamp'])
        elapsed = int(result['elapsed'])
        success = result['success'] == 'true'
        
        sampler_data[sampler].append({
            'timestamp': timestamp,
            'elapsed': elapsed,
            'success': success,
            'bytes': int(result['bytes']) if result['bytes'] else 0
        })
        
        if not success:
            error_data[sampler].append(result)
    
    # 总体统计
    total_requests = len(results)
    total_errors = len([r for r in results if r['success'] != 'true'])
    total_bytes = sum(int(r['bytes']) if r['bytes'] else 0 for r in results)
    
    if results:
        timestamps = [int(r['timeStamp']) for r in results]
        start_time = min(timestamps)
        end_time = max(timestamps)
        duration = (end_time - start_time) / 1000.0  # 转换为秒
        
        analysis['summary'] = {
            'total_requests': total_requests,
            'total_errors': total_errors,
            'error_rate': (total_errors / total_requests * 100) if total_requests > 0 else 0,
            'throughput': total_requests / duration if duration > 0 else 0,
            'total_bytes': total_bytes,
            'duration': duration,
            'start_time': datetime.fromtimestamp(start_time/1000).strftime('%Y-%m-%d %H:%M:%S'),
            'end_time': datetime.fromtimestamp(end_time/1000).strftime('%Y-%m-%d %H:%M:%S')
        }
    
    # 按采样器分析
    for sampler, data in sampler_data.items():
        response_times = [d['elapsed'] for d in data]
        errors = [d for d in data if not d['success']]
        
        analysis['by_sampler'][sampler] = {
            'requests': len(data),
            'errors': len(errors),
            'error_rate': (len(errors) / len(data) * 100) if data else 0,
            'avg_response_time': statistics.mean(response_times) if response_times else 0,
            'min_response_time': min(response_times) if response_times else 0,
            'max_response_time': max(response_times) if response_times else 0,
            'p50_response_time': statistics.median(response_times) if response_times else 0,
            'p90_response_time': sorted(response_times)[int(len(response_times) * 0.9)] if response_times else 0,
            'p95_response_time': sorted(response_times)[int(len(response_times) * 0.95)] if response_times else 0,
            'p99_response_time': sorted(response_times)[int(len(response_times) * 0.99)] if response_times else 0
        }
    
    # 生成建议
    analysis['recommendations'] = generate_recommendations(analysis)
    
    return analysis

def generate_recommendations(analysis):
    """生成性能优化建议"""
    
    recommendations = []
    
    # 错误率分析
    if analysis['summary']['error_rate'] > 5:
        recommendations.append({
            'type': 'error',
            'priority': 'high',
            'message': f'错误率过高 ({analysis["summary"]["error_rate"]:.2f}%)，建议检查系统稳定性和错误处理机制'
        })
    elif analysis['summary']['error_rate'] > 1:
        recommendations.append({
            'type': 'error',
            'priority': 'medium',
            'message': f'错误率偏高 ({analysis["summary"]["error_rate"]:.2f}%)，建议优化错误处理逻辑'
        })
    
    # 响应时间分析
    for sampler, data in analysis['by_sampler'].items():
        if data['avg_response_time'] > 5000:
            recommendations.append({
                'type': 'response_time',
                'priority': 'high',
                'message': f'{sampler} 平均响应时间过长 ({data["avg_response_time"]:.0f}ms)，建议优化数据库查询和业务逻辑'
            })
        elif data['avg_response_time'] > 2000:
            recommendations.append({
                'type': 'response_time',
                'priority': 'medium',
                'message': f'{sampler} 平均响应时间较长 ({data["avg_response_time"]:.0f}ms)，建议进行性能优化'
            })
        
        if data['p95_response_time'] > 10000:
            recommendations.append({
                'type': 'response_time',
                'priority': 'high',
                'message': f'{sampler} P95响应时间过长 ({data["p95_response_time"]:.0f}ms)，建议检查性能瓶颈'
            })
    
    # 吞吐量分析
    if analysis['summary']['throughput'] < 10:
        recommendations.append({
            'type': 'throughput',
            'priority': 'medium',
            'message': f'系统吞吐量较低 ({analysis["summary"]["throughput"]:.2f} req/s)，建议优化并发处理能力'
        })
    
    return recommendations

def generate_html_report(analysis, output_file):
    """生成HTML报告"""
    
    html_content = f"""<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>老人监护管理系统性能分析报告</title>
    <style>
        body {{
            font-family: 'Microsoft YaHei', Arial, sans-serif;
            margin: 0;
            padding: 20px;
            background-color: #f5f5f5;
        }}
        .container {{
            max-width: 1200px;
            margin: 0 auto;
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }}
        .header {{
            text-align: center;
            margin-bottom: 30px;
            padding-bottom: 20px;
            border-bottom: 2px solid #e0e0e0;
        }}
        .header h1 {{
            color: #2c3e50;
            margin-bottom: 10px;
        }}
        .header p {{
            color: #7f8c8d;
            font-size: 14px;
        }}
        .section {{
            margin-bottom: 30px;
        }}
        .section h2 {{
            color: #34495e;
            border-bottom: 2px solid #3498db;
            padding-bottom: 10px;
            margin-bottom: 20px;
        }}
        .summary-grid {{
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-bottom: 20px;
        }}
        .summary-card {{
            background: #f8f9fa;
            padding: 20px;
            border-radius: 8px;
            text-align: center;
            border-left: 4px solid #3498db;
        }}
        .summary-card h3 {{
            margin: 0 0 10px 0;
            color: #2c3e50;
            font-size: 14px;
        }}
        .summary-card .value {{
            font-size: 24px;
            font-weight: bold;
            color: #3498db;
        }}
        .table {{
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 20px;
        }}
        .table th, .table td {{
            padding: 12px;
            text-align: left;
            border-bottom: 1px solid #e0e0e0;
        }}
        .table th {{
            background-color: #f8f9fa;
            font-weight: bold;
            color: #2c3e50;
        }}
        .table tr:hover {{
            background-color: #f8f9fa;
        }}
        .recommendation {{
            padding: 15px;
            margin-bottom: 10px;
            border-radius: 5px;
            border-left: 4px solid;
        }}
        .recommendation.high {{
            border-left-color: #e74c3c;
            background-color: #fdf2f2;
        }}
        .recommendation.medium {{
            border-left-color: #f39c12;
            background-color: #fef9e7;
        }}
        .recommendation.low {{
            border-left-color: #27ae60;
            background-color: #f2f9f4;
        }}
        .recommendation .priority {{
            font-weight: bold;
            text-transform: uppercase;
            margin-bottom: 5px;
        }}
        .recommendation.high .priority {{
            color: #e74c3c;
        }}
        .recommendation.medium .priority {{
            color: #f39c12;
        }}
        .recommendation.low .priority {{
            color: #27ae60;
        }}
        .status-indicator {{
            display: inline-block;
            width: 12px;
            height: 12px;
            border-radius: 50%;
            margin-right: 8px;
        }}
        .status-good {{
            background-color: #27ae60;
        }}
        .status-warning {{
            background-color: #f39c12;
        }}
        .status-error {{
            background-color: #e74c3c;
        }}
        .footer {{
            text-align: center;
            margin-top: 30px;
            padding-top: 20px;
            border-top: 1px solid #e0e0e0;
            color: #7f8c8d;
            font-size: 12px;
        }}
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>老人监护管理系统性能分析报告</h1>
            <p>生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}</p>
        </div>
        
        <div class="section">
            <h2>测试概览</h2>
            <div class="summary-grid">
                <div class="summary-card">
                    <h3>总请求数</h3>
                    <div class="value">{analysis['summary'].get('total_requests', 0):,}</div>
                </div>
                <div class="summary-card">
                    <h3>错误率</h3>
                    <div class="value">{analysis['summary'].get('error_rate', 0):.2f}%</div>
                </div>
                <div class="summary-card">
                    <h3>吞吐量</h3>
                    <div class="value">{analysis['summary'].get('throughput', 0):.2f} req/s</div>
                </div>
                <div class="summary-card">
                    <h3>测试时长</h3>
                    <div class="value">{analysis['summary'].get('duration', 0):.1f}s</div>
                </div>
            </div>
        </div>
        
        <div class="section">
            <h2>各接口性能详情</h2>
            <table class="table">
                <thead>
                    <tr>
                        <th>接口名称</th>
                        <th>请求数</th>
                        <th>错误率</th>
                        <th>平均响应时间</th>
                        <th>最小响应时间</th>
                        <th>最大响应时间</th>
                        <th>P95响应时间</th>
                        <th>状态</th>
                    </tr>
                </thead>
                <tbody>"""
    
    for sampler, data in analysis['by_sampler'].items():
        # 确定状态
        if data['error_rate'] > 5 or data['avg_response_time'] > 5000:
            status = '<span class="status-indicator status-error"></span>需要优化'
        elif data['error_rate'] > 1 or data['avg_response_time'] > 2000:
            status = '<span class="status-indicator status-warning"></span>需要关注'
        else:
            status = '<span class="status-indicator status-good"></span>良好'
        
        html_content += f"""
                    <tr>
                        <td>{sampler}</td>
                        <td>{data['requests']:,}</td>
                        <td>{data['error_rate']:.2f}%</td>
                        <td>{data['avg_response_time']:.0f}ms</td>
                        <td>{data['min_response_time']:.0f}ms</td>
                        <td>{data['max_response_time']:.0f}ms</td>
                        <td>{data['p95_response_time']:.0f}ms</td>
                        <td>{status}</td>
                    </tr>"""
    
    html_content += """
                </tbody>
            </table>
        </div>
        
        <div class="section">
            <h2>性能优化建议</h2>"""
    
    if analysis['recommendations']:
        for rec in analysis['recommendations']:
            html_content += f"""
            <div class="recommendation {rec['priority']}">
                <div class="priority">{rec['priority']}优先级</div>
                <div>{rec['message']}</div>
            </div>"""
    else:
        html_content += """
            <p>系统性能表现良好，暂无优化建议。</p>"""
    
    html_content += f"""
        </div>
        
        <div class="section">
            <h2>测试时间范围</h2>
            <p><strong>开始时间:</strong> {analysis['summary'].get('start_time', 'N/A')}</p>
            <p><strong>结束时间:</strong> {analysis['summary'].get('end_time', 'N/A')}</p>
            <p><strong>数据传输量:</strong> {analysis['summary'].get('total_bytes', 0) / 1024 / 1024:.2f} MB</p>
        </div>
        
        <div class="footer">
            <p>老人监护管理系统性能分析报告 | 自动生成</p>
        </div>
    </div>
</body>
</html>"""
    
    # 写入文件
    try:
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write(html_content)
        return True
    except Exception as e:
        print(f"写入HTML文件失败: {e}")
        return False

def main():
    """主函数"""
    
    if len(sys.argv) != 3:
        print("用法: python analyze-performance.py <jtl文件> <输出html文件>")
        sys.exit(1)
    
    jtl_file = sys.argv[1]
    output_file = sys.argv[2]
    
    print(f"分析性能数据: {jtl_file}")
    print(f"生成报告: {output_file}")
    
    if analyze_performance(jtl_file, output_file):
        print("性能分析完成！")
    else:
        print("性能分析失败！")
        sys.exit(1)

if __name__ == "__main__":
    main()