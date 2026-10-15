#!/usr/bin/env python3
"""Runs after `npx cap add android`: adds native code, permissions, icons and release signing."""
import os, re, shutil, sys
root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A = os.path.join(root, 'android')
pkg = os.path.join(A, 'app/src/main/java/com/mizou/fieldplanner')
for f in ('MainActivity.java', 'ClockAlarmPlugin.java', 'CarBtReceiver.java'):
    shutil.copy(os.path.join(root, 'tools/native', f), os.path.join(pkg, f))

man = os.path.join(A, 'app/src/main/AndroidManifest.xml')
m = open(man, encoding='utf-8').read()
perms = ['CAMERA', 'POST_NOTIFICATIONS', 'SCHEDULE_EXACT_ALARM', 'USE_EXACT_ALARM', 'RECEIVE_BOOT_COMPLETED', 'VIBRATE',
         'WAKE_LOCK', 'BLUETOOTH_CONNECT', 'ACCESS_FINE_LOCATION', 'ACCESS_COARSE_LOCATION', 'FOREGROUND_SERVICE', 'FOREGROUND_SERVICE_LOCATION', 'REQUEST_IGNORE_BATTERY_OPTIMIZATIONS', 'com.android.alarm.permission.SET_ALARM']
add = ''
for p in perms:
    n = p if '.' in p else 'android.permission.' + p
    if n not in m:
        add += '    <uses-permission android:name="%s" />\n' % n
add += '    <uses-feature android:name="android.hardware.camera" android:required="false" />\n'
add += '    <queries><intent><action android:name="android.intent.action.SET_ALARM" /></intent><package android:name="com.whatsapp" /><package android:name="com.whatsapp.w4b" /></queries>\n'
seen=set()
lines=[]
for ln in add.splitlines(True):
    if ln in seen:
        continue
    seen.add(ln); lines.append(ln)
add=''.join(lines)
assert '</manifest>' in m
m = m.replace('</manifest>', add + '</manifest>')
m = m.replace('<activity', '<activity android:screenOrientation="portrait"', 1) if 'screenOrientation' not in m else m
open(man, 'w', encoding='utf-8').write(m)

m = open(man, encoding='utf-8').read()
m = m.replace('android:allowBackup="true"', 'android:allowBackup="false"')
if 'usesCleartextTraffic' not in m:
    m = m.replace('<application', '<application android:usesCleartextTraffic="false"', 1)
if 'CarBtReceiver' not in m:
    m = m.replace('</application>', '    <receiver android:name=".CarBtReceiver" android:exported="true">\n            <intent-filter>\n                <action android:name="android.bluetooth.device.action.ACL_CONNECTED" />\n                <action android:name="android.bluetooth.device.action.ACL_DISCONNECTED" />\n            </intent-filter>\n        </receiver>\n    </application>', 1)
open(man, 'w', encoding='utf-8').write(m)

res = os.path.join(A, 'app/src/main/res')
shutil.rmtree(os.path.join(res, 'mipmap-anydpi-v26'), ignore_errors=True)
src = os.path.join(root, 'tools/res')
for d in os.listdir(src):
    dst = os.path.join(res, d)
    os.makedirs(dst, exist_ok=True)
    for f in os.listdir(os.path.join(src, d)):
        shutil.copy(os.path.join(src, d, f), os.path.join(dst, f))

# the Google sign-in plugin still lists the retired jcenter() repository, which can break the build
for dp, dn, fn in os.walk(os.path.join(root, 'node_modules')):
    if 'capacitor-google-auth' in dp and 'build.gradle' in fn:
        gp = os.path.join(dp, 'build.gradle')
        tx = open(gp, encoding='utf-8').read()
        if 'jcenter()' in tx:
            open(gp, 'w', encoding='utf-8').write(tx.replace('jcenter()', 'mavenCentral()'))
            print('fixed jcenter in', gp)

import json
gid = ''
gp = os.path.join(root, 'google-client-id.txt')
if os.path.exists(gp):
    ls = [l.strip() for l in open(gp, encoding='utf-8') if l.strip() and not l.strip().startswith('#')]
    gid = ls[0] if ls else ''
open(os.path.join(root, 'www', 'config.js'), 'w', encoding='utf-8').write('window.WT_CONFIG=' + json.dumps({'gid': gid}) + ';')
print('google client id in config:', 'yes' if gid else 'no')

g = os.path.join(A, 'app/build.gradle')
t = open(g, encoding='utf-8').read()
t = t.replace('versionCode 1', 'versionCode ' + os.environ.get('GITHUB_RUN_NUMBER', '1'), 1)
t += '''
dependencies {
    implementation 'androidx.biometric:biometric:1.1.0'
}
android {
    signingConfigs {
        release {
            storeFile file("${rootProject.projectDir}/../signing/release.jks")
            storePassword 'fieldplanner'
            keyAlias 'fdp'
            keyPassword 'fieldplanner'
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
        }
    }
}
'''
open(g, 'w', encoding='utf-8').write(t)
print('patched android project')
