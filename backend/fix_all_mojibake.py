# -*- coding: utf-8 -*-
"""Fix mojibake (garbled Chinese) in source files."""
import os
import re

BASE = "src/main/java"
TEST_BASE = "src/test/java"

def fix(path, replacements):
    full = os.path.join(os.path.dirname(__file__), path)
    with open(full, "r", encoding="utf-8") as f:
        text = f.read()
    for pat, repl in replacements:
        text = re.sub(pat, repl, text)
    with open(full, "w", encoding="utf-8") as f:
        f.write(text)

# DashboardService.java
fix(os.path.join(BASE, "com/prm/module/dashboard/application/DashboardService.java"), [
    (r'throw BizException\.forbidden\("浠呴」鐩[^"]*粡鐞嗗彲瑙﹀彂鎵€灞為」鐩[^"]*仛鍚\?"\)', 'throw BizException.forbidden("仅项目经理可触发看板项目");'),
])

# TaskService.java - notFound 可能含特殊字符
fix(os.path.join(BASE, "com/prm/module/task/application/TaskService.java"), [
    (r'throw BizException\.notFound\("娴犺[^"]*濮\?"\)', 'throw BizException.notFound("任务");'),
    (r'throw BizException\.forbidden\("閺冪姵娼堥弻銉ф箙鐠囥儰鎹㈤崝\?"\)', 'throw BizException.forbidden("无项目查看权限");'),
    (r'throw BizException\.forbidden\("娴犲懎褰查弻銉ф箙閸掑棝鍘ょ紒娆掑殰瀹歌京娈戞禒璇插[^"]*"\)', 'throw BizException.forbidden("仅项目负责人或任务指派人可查看");'),
    (r'throw BizException\.forbidden\("閺冪姵娼堥幙宥勭稊鐠囥儰鎹㈤崝\?"\)', 'throw BizException.forbidden("无项目编辑权限");'),
    (r'throw BizException\.forbidden\("娴犲懎褰查幙宥勭稊閸掑棝鍘ょ紒娆掑殰瀹歌京娈戞禒璇插[^"]*"\)', 'throw BizException.forbidden("仅项目负责人或任务指派人可操作");'),
    (r'throw BizException\.forbidden\("閸欘亝婀[^"]*"\)', 'throw BizException.forbidden("仅项目经理可操作");'),
])

# SprintService.java - notFound already fixed; fix of() and forbidden
fix(os.path.join(BASE, "com/prm/module/sprint/application/SprintService.java"), [
    (r'throw BizException\.of\("鐎涙ê婀\?" \+ openCritical \+ " [^"]*"\)', 'throw BizException.of("当前有 " + openCritical + " 个严重Bug未解决，请先解决后再关闭迭代");'),
    (r'throw BizException\.forbidden\("閺冪姵娼堥弻銉ф箙鐠囥儴鍑[^"]*"\)', 'throw BizException.forbidden("无项目关闭权限");'),
    (r'throw BizException\.forbidden\("娴犲懘銆嶉惄顔剧病[^"]*鏉╊厺鍞\?"\)', 'throw BizException.forbidden("仅项目经理可关闭迭代");'),
])

# ReleaseService.java - notFound already fixed
fix(os.path.join(BASE, "com/prm/module/release/application/ReleaseService.java"), [
    (r'throw BizException\.of\("閸欘亝婀侀懡澶岊焾[^"]*"\)', 'throw BizException.of("版本非草稿状态无法发布");'),
    (r'throw BizException\.forbidden\("閺冪姵娼堥弻銉ф箙鐠囥儳澧楅張\?"\)', 'throw BizException.forbidden("无项目查看权限");'),
    (r'throw BizException\.forbidden\("娴犲懘銆嶉惄顔剧病[^"]*閻楀牊婀\?"\)', 'throw BizException.forbidden("仅项目经理可查看版本");'),
])

# ProjectServicePermissionTests.java
fix(os.path.join(TEST_BASE, "com/prm/module/project/ProjectServicePermissionTests.java"), [
    (r'\.hasMessageContaining\("鏌ョ湅"\)', '.hasMessageContaining("查看")'),
])

# TaskService notFound - already fixed (was missing closing quote: ?");  -> ?);)

print("Done.")
