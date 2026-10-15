package com.mizou.fieldplanner;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.provider.AlarmClock;
import android.provider.Settings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.util.Base64;
import android.content.ContentValues;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import java.io.OutputStream;
import java.util.concurrent.Executor;
import android.view.WindowManager;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import com.getcapacitor.PermissionState;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;

import java.util.ArrayList;

@CapacitorPlugin(name = "ClockAlarm", permissions = { @Permission(strings = { "android.permission.BLUETOOTH_CONNECT" }, alias = "bluetooth") })
public class ClockAlarmPlugin extends Plugin {
    private WebView printView;

    @PluginMethod
    public void setAlarm(PluginCall call) {
        try {
            Intent i = new Intent(AlarmClock.ACTION_SET_ALARM);
            i.putExtra(AlarmClock.EXTRA_HOUR, call.getInt("hour", 7));
            i.putExtra(AlarmClock.EXTRA_MINUTES, call.getInt("minute", 0));
            i.putExtra(AlarmClock.EXTRA_MESSAGE, call.getString("label", "Field Planner"));
            i.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
            JSArray days = call.getArray("days");
            if (days != null && days.length() > 0) {
                ArrayList<Integer> list = new ArrayList<>();
                for (int k = 0; k < days.length(); k++) list.add(days.getInt(k));
                i.putExtra(AlarmClock.EXTRA_DAYS, list);
            }
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(i);
            call.resolve();
        } catch (Exception e) {
            call.reject("Could not set the alarm: " + e.getMessage());
        }
    }

    @PluginMethod
    public void requestBatteryExemption(PluginCall call) {
        try {
            Context c = getContext();
            String pkg = c.getPackageName();
            PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
            JSObject r = new JSObject();
            if (pm != null && pm.isIgnoringBatteryOptimizations(pkg)) {
                r.put("already", true);
                call.resolve(r);
                return;
            }
            Intent i = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + pkg));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                c.startActivity(i);
            } catch (Exception e) {
                Intent j = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                j.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                c.startActivity(j);
            }
            r.put("already", false);
            call.resolve(r);
        } catch (Exception e) {
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void openAppSettings(PluginCall call) {
        try {
            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getContext().getPackageName()));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(i);
            call.resolve();
        } catch (Exception e) {
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void printHtml(final PluginCall call) {
        final String html = call.getString("html", "");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    printView = new WebView(getContext());
                    printView.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onPageFinished(WebView view, String url) {
                            PrintManager pm = (PrintManager) getActivity().getSystemService(Context.PRINT_SERVICE);
                            String name = "Fees sheet";
                            PrintDocumentAdapter adapter = view.createPrintDocumentAdapter(name);
                            PrintAttributes attrs = new PrintAttributes.Builder()
                                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                                    .build();
                            pm.print(name, adapter, attrs);
                        }
                    });
                    printView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
                    call.resolve();
                } catch (Exception e) {
                    call.reject("Could not open print: " + e.getMessage());
                }
            }
        });
    }

    private boolean hasPkg(String pkg) {
        try {
            getContext().getPackageManager().getPackageInfo(pkg, 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @PluginMethod
    public void whatsappInstalled(PluginCall call) {
        JSObject r = new JSObject();
        r.put("messenger", hasPkg("com.whatsapp"));
        r.put("business", hasPkg("com.whatsapp.w4b"));
        call.resolve(r);
    }

    @PluginMethod
    public void openWhatsApp(PluginCall call) {
        try {
            String pkg = call.getString("pkg", "com.whatsapp");
            String num = call.getString("number", "");
            String text = call.getString("text", "");
            Uri u = Uri.parse("https://wa.me/" + num + "?text=" + Uri.encode(text));
            Intent i = new Intent(Intent.ACTION_VIEW, u);
            i.setPackage(pkg);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(i);
            call.resolve();
        } catch (Exception e) {
            call.reject("WhatsApp could not open: " + e.getMessage());
        }
    }

    @PluginMethod
    public void sendPdf(PluginCall call) {
        try {
            String b64 = call.getString("base64", "");
            String name = call.getString("fileName", "catalog.pdf").replaceAll("[^A-Za-z0-9._-]", "_");
            String pkg = call.getString("pkg", "");
            String num = call.getString("number", "");
            String text = call.getString("text", "");
            byte[] data = Base64.decode(b64, Base64.DEFAULT);
            File dir = new File(getContext().getCacheDir(), "shared");
            dir.mkdirs();
            File f = new File(dir, name);
            FileOutputStream o = new FileOutputStream(f);
            o.write(data);
            o.close();
            Uri uri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileprovider", f);
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType(call.getString("mime", "application/pdf"));
            i.putExtra(Intent.EXTRA_STREAM, uri);
            i.putExtra(Intent.EXTRA_TEXT, text);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (pkg != null && pkg.length() > 0) {
                i.setPackage(pkg);
                if (num != null && num.length() > 0) i.putExtra("jid", num + "@s.whatsapp.net");
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(i);
            } else {
                Intent c = Intent.createChooser(i, "Share PDF");
                c.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(c);
            }
            call.resolve();
        } catch (Exception e) {
            call.reject("Could not share the PDF: " + e.getMessage());
        }
    }

    @PluginMethod
    public void batteryStatus(PluginCall call) {
        JSObject r = new JSObject();
        try {
            PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
            r.put("ignoring", pm != null && pm.isIgnoringBatteryOptimizations(getContext().getPackageName()));
        } catch (Exception e) {
            r.put("ignoring", true);
        }
        call.resolve(r);
    }

    @PluginMethod
    public void saveFile(PluginCall call) {
        try {
            String b64 = call.getString("base64", "");
            String name = call.getString("fileName", "backup.json").replaceAll("[^A-Za-z0-9._-]", "_");
            String mime = call.getString("mime", "application/octet-stream");
            byte[] data = Base64.decode(b64, Base64.DEFAULT);
            JSObject r = new JSObject();
            if (Build.VERSION.SDK_INT >= 29) {
                ContentValues v = new ContentValues();
                v.put(MediaStore.Downloads.DISPLAY_NAME, name);
                v.put(MediaStore.Downloads.MIME_TYPE, mime);
                v.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/WorkTracker");
                Uri u = getContext().getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v);
                OutputStream o = getContext().getContentResolver().openOutputStream(u);
                o.write(data);
                o.close();
                r.put("path", "Downloads/WorkTracker/" + name);
            } else {
                File dir = new File(getContext().getExternalFilesDir(null), "backups");
                dir.mkdirs();
                File f = new File(dir, name);
                FileOutputStream o = new FileOutputStream(f);
                o.write(data);
                o.close();
                r.put("path", f.getAbsolutePath());
            }
            call.resolve(r);
        } catch (Exception e) {
            call.reject("Could not save the file: " + e.getMessage());
        }
    }

    @PluginMethod
    public void biometricStatus(PluginCall call) {
        JSObject r = new JSObject();
        try {
            int s = BiometricManager.from(getContext()).canAuthenticate(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
            r.put("available", s == BiometricManager.BIOMETRIC_SUCCESS);
        } catch (Exception e) {
            r.put("available", false);
        }
        call.resolve(r);
    }

    @PluginMethod
    public void authenticate(final PluginCall call) {
        final String title = call.getString("title", "Work Tracker");
        final String sub = call.getString("subtitle", "Confirm it is you");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Executor ex = ContextCompat.getMainExecutor(getContext());
                    BiometricPrompt bp = new BiometricPrompt(getActivity(), ex, new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                            call.resolve();
                        }

                        @Override
                        public void onAuthenticationError(int errorCode, CharSequence errString) {
                            call.reject(String.valueOf(errString));
                        }
                    });
                    BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                            .setTitle(title)
                            .setSubtitle(sub)
                            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                            .build();
                    bp.authenticate(info);
                } catch (Exception e) {
                    call.reject("Authentication is not available: " + e.getMessage());
                }
            }
        });
    }

    @PluginMethod
    public void setSecure(final PluginCall call) {
        final boolean on = Boolean.TRUE.equals(call.getBoolean("on", false));
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (on) {
                    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
                } else {
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
                }
                call.resolve();
            }
        });
    }

    @PluginMethod
    public void btPaired(PluginCall call) {
        if (Build.VERSION.SDK_INT >= 31 && getPermissionState("bluetooth") != PermissionState.GRANTED) {
            requestPermissionForAlias("bluetooth", call, "btCb");
            return;
        }
        btList(call);
    }

    @PermissionCallback
    private void btCb(PluginCall call) {
        if (Build.VERSION.SDK_INT < 31 || getPermissionState("bluetooth") == PermissionState.GRANTED) {
            btList(call);
        } else {
            call.reject("Bluetooth permission was not allowed");
        }
    }

    private void btList(PluginCall call) {
        try {
            BluetoothAdapter a = BluetoothAdapter.getDefaultAdapter();
            JSArray arr = new JSArray();
            if (a != null) {
                for (BluetoothDevice d : a.getBondedDevices()) {
                    JSObject o = new JSObject();
                    o.put("name", d.getName());
                    o.put("address", d.getAddress());
                    arr.put(o);
                }
            }
            JSObject r = new JSObject();
            r.put("devices", arr);
            call.resolve(r);
        } catch (Exception e) {
            call.reject("Could not read paired devices: " + e.getMessage());
        }
    }

    @PluginMethod
    public void setCarDevice(PluginCall call) {
        SharedPreferences.Editor ed = getContext().getSharedPreferences("wt_car", Context.MODE_PRIVATE).edit();
        ed.putString("addr", call.getString("address", ""));
        ed.putString("name", call.getString("name", ""));
        ed.apply();
        call.resolve();
    }

    @PluginMethod
    public void carStatus(PluginCall call) {
        try {
            SharedPreferences p = getContext().getSharedPreferences("wt_car", Context.MODE_PRIVATE);
            JSObject r = new JSObject();
            r.put("name", p.getString("name", ""));
            r.put("address", p.getString("addr", ""));
            JSArray ev = new JSArray();
            for (String part : p.getString("events", "").split(";")) {
                if (part.length() == 0) continue;
                String[] x = part.split("\\|");
                if (x.length == 2) {
                    JSArray e = new JSArray();
                    e.put(Long.parseLong(x[0]));
                    e.put(Integer.parseInt(x[1]));
                    ev.put(e);
                }
            }
            r.put("events", ev);
            call.resolve(r);
        } catch (Exception e) {
            call.reject("Could not read the car status: " + e.getMessage());
        }
    }
}
