#!/bin/bash

#=======================================================================================
# 应用管理脚本
# 功能：Start | Stop | Restart | Status
# 适用：Java -cp / -classpath 启动方式、支持无 pgrep/pwdx 命令的精简 Linux 环境
# 特点：基于路径精准定位 PID，支持多应用同机部署，支持优雅停机
#=======================================================================================

# 1. 基础配置
# 如果没有配置 JAVA_HOME，尝试使用默认路径或报错
if [ -z "$JAVA_HOME" ]; then
    JAVA_CMD="java"
else
    JAVA_CMD="$JAVA_HOME/bin/java"
fi

# 【必须修改】主类名称 (包含包名)，例如：com.example.YourMainClass
APP_MAIN="com.example.YourMainClass"

# JVM 参数配置
JVM_OPTS="-Dname=$APP_MAIN -Duser.timezone=Asia/Shanghai -Xms512m -Xmx512m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m"

# 自动获取当前脚本所在的绝对路径作为应用目录
APP_HOME=$(cd "$(dirname "$0")" && pwd)

# 日志配置
LOG_PATH="$APP_HOME/logs/app.log"
mkdir -p "$APP_HOME/logs"

# 构建 Classpath (自动包含当前目录、lib下所有jar、config目录)
# 如果你的启动方式很特殊，可以直接修改下面的 CLASSPATH 逻辑
CLASSPATH="$APP_HOME"
for jar in $APP_HOME/lib/*.jar; do
    CLASSPATH="$CLASSPATH:$jar"
done
for jar in $APP_HOME/dist/*.jar; do
    CLASSPATH="$CLASSPATH:$jar"
done
if [ -d "$APP_HOME/config" ]; then
    CLASSPATH="$APP_HOME/config:$CLASSPATH"
fi

# 全局变量：PID
APP_PID=0

#=======================================================================================
# 函数：get_pid
# 说明：精准获取当前应用目录下的 Java 进程 PID
# 逻辑：1. 找出所有 java 进程 -> 2. 检查进程的工作目录(pwdx)是否等于 $APP_HOME
#=======================================================================================
get_pid() {
    APP_PID=0

    # 1. 通过主类名找 PID
    local pids=$(ps -ef | grep "$APP_MAIN" | grep -v grep | awk '{print $2}')

    # 没找到直接返回
    [[ -z "$pids" ]] && return

    # # 遍历候选 PID，校验工作目录
    for pid in $pids; do
        # 优先用 pwdx，不存在则回退到 /proc 方式
        local dir=$(pwdx "$pid" 2>/dev/null | awk '{print $2}')
        [[ -z "$dir" ]] && dir=$(readlink /proc/$pid/cwd 2>/dev/null)

        # 目录匹配则锁定
        if [[ -n "$dir" && "$dir" == "$APP_HOME" ]]; then
            APP_PID=$pid
            break
        fi
    done
}

#=======================================================================================
# 命令：start
#=======================================================================================
start() {
    get_pid
    if [ $APP_PID -ne 0 ]; then
        echo "$APP_NAME already started (PID=$APP_PID)"
    else
        echo -n "Starting $APP_NAME ... "
        # 使用 nohup 启动，输出重定向到日志文件
        nohup $JAVA_CMD $JVM_OPTS -classpath "$CLASSPATH" $APP_MAIN > $LOG_PATH 2>&1 &

        # 等待几秒让进程启动
        sleep 2
        get_pid

        if [ $APP_PID -ne 0 ]; then
            echo "[Success] (PID=$APP_PID)"
            echo "Log path: $LOG_PATH"
        else
            echo "[Failed] Please check the log file for details"
            echo "Log path: $LOG_PATH"
        fi
    fi
}

#=======================================================================================
# 命令：stop
#=======================================================================================
stop() {
    get_pid
    if [ $APP_PID -eq 0 ]; then
        echo "$APP_MAIN is not running"
    else
        echo -n "Stopping $APP_MAIN (PID=$APP_PID) ... "
        kill -TERM $APP_PID

        # 等待进程退出 (最多 30 秒)
        local wait_count=0
        while [ $wait_count -lt 30 ]; do
            get_pid
            if [ $APP_PID -eq 0 ]; then
                echo "[Success]"
                return 0
            fi
            sleep 1
            wait_count=$((wait_count + 1))
        done

        # 超时强制杀死
        echo "Timeout reached. Forcing kill..."
        kill -9 $APP_PID
        sleep 1
        get_pid
        if [ $APP_PID -eq 0 ]; then
            echo "[Force Stop Success]"
        else
            echo "[Failed] Please check manually"
        fi
    fi
}

#=======================================================================================
# 命令：status
#=======================================================================================
status() {
    get_pid
    if [ $APP_PID -ne 0 ]; then
        echo "$APP_NAME is running... (PID=$APP_PID)"
    else
        echo "$APP_NAME is not running..."
    fi
}

#=======================================================================================
# 命令：restart
#=======================================================================================
restart() {
    stop
    # 确保停干净了再启
    sleep 3
    start
}


#=======================================================================================
# 主入口
#=======================================================================================
case "$1" in
    'start')
        start
        ;;
    'stop')
        stop
        ;;
    'restart')
        restart
        ;;
    'status')
        status
        ;;
    *)
        echo "用法: $0 {start|stop|restart|status}"
        exit 1
        ;;
esac

exit 0