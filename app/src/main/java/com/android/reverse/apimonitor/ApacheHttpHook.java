package com.android.reverse.apimonitor;

import android.annotation.TargetApi;

import com.android.reverse.hook.HookParam;
import com.android.reverse.util.Logger;
import com.android.reverse.util.RefInvoke;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.lang.reflect.Method;


public class ApacheHttpHook extends ApiMonitorHook {

    @Override
    public void startHook() {

        Method executeRequest = RefInvoke.findMethodExact("org.apache.http.impl.client.AbstractHttpClient", ClassLoader.getSystemClassLoader(),
                "execute", HttpHost.class, HttpRequest.class, HttpContext.class);

        hookhelper.hookMethod(executeRequest, new AbstractBahaviorHookCallBack() {
            @Override
            public void descParam(HookParam param) {
                Logger.log_behavior("Connect to URL ->");
                HttpHost host = (HttpHost) param.args[0];
                HttpRequest request = (HttpRequest) param.args[1];
                if (request instanceof HttpGet) {
                    HttpGet httpGet = (HttpGet) request;
                    Logger.log_behavior("HTTP Method : " + httpGet.getMethod());
                    Logger.log_behavior("HTTP URL : " + httpGet.getURI().toString());
                    Header[] headers = request.getAllHeaders();
                    if (headers != null) {
                        for (int i = 0; i < headers.length; i++) {
                            Logger.log_behavior(headers[i].getName() + ":" + headers[i].getName());
                        }
                    }
                } else if (request instanceof HttpPost) {
                    HttpPost httpPost = (HttpPost) request;
                    Logger.log_behavior("HTTP Method : " + httpPost.getMethod());
                    Logger.log_behavior("HTTP URL : " + httpPost.getURI().toString());
                    Header[] headers = request.getAllHeaders();
                    if (headers != null) {
                        for (int i = 0; i < headers.length; i++) {
                            Logger.log_behavior(headers[i].getName() + ":" + headers[i].getValue());
                        }
                    }
                    HttpEntity entity = httpPost.getEntity();
                    String contentType = null;
                    if (entity.getContentType() != null) {
                        contentType = entity.getContentType().getValue();
                        if (URLEncodedUtils.CONTENT_TYPE.equals(contentType)) {

                            try {
                                byte[] data = new byte[(int) entity.getContentLength()];
                                entity.getContent().read(data);
                                String content = new String(data, HTTP.DEFAULT_CONTENT_CHARSET);
                                Logger.log_behavior("HTTP POST Content : " + content);
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        } else if (contentType.startsWith(HTTP.DEFAULT_CONTENT_TYPE)) {
                            try {
                                byte[] data = new byte[(int) entity.getContentLength()];
                                entity.getContent().read(data);
                                String content = new String(data, contentType.substring(contentType.lastIndexOf("=") + 1));
                                Logger.log_behavior("HTTP POST Content : " + content);
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    } else {
                        byte[] data = new byte[(int) entity.getContentLength()];
                        try {
                            entity.getContent().read(data);
                            String content = new String(data, HTTP.DEFAULT_CONTENT_CHARSET);
                            Logger.log_behavior("HTTP POST Content : " + content);
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                }
            }

            @Override
            public void afterHookedMethod(HookParam param) {
                super.afterHookedMethod(param);
                HttpResponse resp = (HttpResponse) param.getResult();
                if (resp != null) {
                    Logger.log_behavior("Status Code = " + resp.getStatusLine().getStatusCode());
                    Header[] headers = resp.getAllHeaders();
                    if (headers != null) {
                        for (int i = 0; i < headers.length; i++) {
                            Logger.log_behavior(headers[i].getName() + ":" + headers[i].getValue());
                        }
                    }

                }
            }

        });
    }


}
