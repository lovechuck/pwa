var cacheName = 'test'

self.addEventListener('install', event => {
    event.waitUntil(
        caches.open(cacheName)
            .then(cache => cache.addAll(['1.jpg']))
    )
})

self.addEventListener('fetch', event => {
    event.respondWith(
        caches.match(event.request)
            .then(resp => {
                if (resp) {
                    return resp;
                }

                var requestToCache = event.request.clone();

                return fetch(requestToCache).then(
                    (response) => {
                        if (!response || response.status != 200) {
                            return response;
                        }

                        var responseToCache = response.clone();

                        caches.open(cacheName).then(cache => {
                            cache.put(requestToCache, responseToCache)
                        })

                        return response;
                    }
                )
            })
    )
})

