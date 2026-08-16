#!/bin/bash
# GitHub热门项目每日自动生成 - 定时任务设置脚本

echo "=========================================="
echo "设置每日自动生成GitHub热门项目Excel"
echo "=========================================="

# 获取当前用户
USER=$(whoami)
SCRIPT_PATH="/Users/stjzzy/WorkBuddy/Claw/daily_github_trending.py"
LOG_PATH="/Users/stjzzy/WorkBuddy/Claw/logs"

# 创建日志目录
mkdir -p "$LOG_PATH"

# 设置定时任务 (每天上午9点运行)
CRON_JOB="0 9 * * * cd /Users/stjzzy/WorkBuddy/Claw && /usr/bin/python3 $SCRIPT_PATH >> $LOG_PATH/github_trending.log 2>&1"

# 检查是否已存在相同的定时任务
if crontab -l 2>/dev/null | grep -q "daily_github_trending.py"; then
    echo "⚠️  定时任务已存在"
    echo ""
    echo "当前定时任务:"
    crontab -l | grep "daily_github_trending.py"
    echo ""
    read -p "是否重新设置? (y/n): " confirm
    if [ "$confirm" != "y" ]; then
        echo "已取消"
        exit 0
    fi
    # 删除旧任务
    crontab -l | grep -v "daily_github_trending.py" | crontab -
fi

# 添加新定时任务
(crontab -l 2>/dev/null; echo "$CRON_JOB") | crontab -

echo "✅ 定时任务已设置成功！"
echo ""
echo "任务详情:"
echo "  运行时间: 每天上午9:00"
echo "  脚本路径: $SCRIPT_PATH"
echo "  日志路径: $LOG_PATH/github_trending.log"
echo ""
echo "手动运行测试:"
echo "  python3 $SCRIPT_PATH"
echo ""
echo "查看定时任务:"
echo "  crontab -l"
echo ""
echo "删除定时任务:"
echo "  crontab -e"
echo "  然后删除包含 daily_github_trending.py 的行"
echo "=========================================="
