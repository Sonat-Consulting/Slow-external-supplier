Slow-external-supplier
=================================

This app fakes a slow external supplier.

It has one API call, placeOrder, which can be fast or slow or both.. and it can fail..


The API-call is like this:

    GET or PUT /placeOrder?itemId=4453

And it will return the following Json:

    {
        orderId: "1005",
        itemId: "4453"
    }


response-time
---------------
The response-time is calculated like this:

    // default: fixedDelayInMills=1000 and randomDelayInMills=1000
    fixedDelayInMills * random(randomDelayInMills)


So by default it will return in between 1 and 2 seconds.


These values can be overrided using query-params.

This will turn all delay off:

    http://localhost:9000/placeOrder?itemId=1212&fixedDelayInMills=0&randomDelayInMills=0


errors
-----------

By default, 30% of all calls will result in 503 Service Unavailable.

This can be modified by using the query-param *errorRateInPercent*
