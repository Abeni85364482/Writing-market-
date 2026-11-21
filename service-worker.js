const CACHE_NAME = "ariti-market-v1";
const BASE_URL = new URL("./", self.location.href);
const APP_SHELL = [BASE_URL.href, new URL("manifest.webmanifest", BASE_URL).href, new URL("app-icon.svg", BASE_URL).href];
self.addEventListener("install", event => {
  event.waitUntil(caches.open(CACHE_NAME).then(cache => cache.addAll(APP_SHELL)).then(() => self.skipWaiting()));
});
self.addEventListener("activate", event => {
  event.waitUntil(caches.keys().then(keys => Promise.all(keys.filter(key => key !== CACHE_NAME).map(key => caches.delete(key)))).then(() => self.clients.claim()));
});
self.addEventListener("fetch", event => {
  const request = event.request;
  const url = new URL(request.url);
  if (request.method !== "GET" || url.origin !== self.location.origin) return;
  if (request.mode === "navigate") {
    event.respondWith(fetch(request).then(response => {
      const copy = response.clone();
      caches.open(CACHE_NAME).then(cache => cache.put(BASE_URL.href, copy));
      return response;
    }).catch(() => caches.match(BASE_URL.href)));
  }
});