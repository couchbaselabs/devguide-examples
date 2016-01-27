using Couchbase;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DevGuide
{
    public class Expiration : ConnectionBase
    {
        public override async Task ExecuteAsync()
        {
            var key = "dotnetDevguideExampleExpiration-" + DateTime.Now.Ticks;

            // Creating a document with an time-to-live (expiration) of 2 seconds
            await _bucket.UpsertAsync(key, "Hello world!", TimeSpan.FromSeconds(2));

            // Retrieving immediately
            var result = await _bucket.GetAsync<string>(key);
            Console.WriteLine("[{0:HH:mm:ss.fff}] Got: '{1}', Status: {2}", DateTime.Now, result.Value, result.Status);

            // Waiting 4 seconds
            await Task.Delay(4000);

            // Retrieving after a 4 second delay
            var result2 = await _bucket.GetAsync<string>(key);
            Console.WriteLine("[{0:HH:mm:ss.fff}] Got: '{1}', Status: {2}", DateTime.Now, result2.Value, result2.Status);


            // Creating an item with 1 second TTL
            await _bucket.UpsertAsync(key, "Hello world!", TimeSpan.FromSeconds(1));

            // Retrieving the item and extending the TTL to 2 seconds with getAndTouch
            var result3 = await _bucket.GetAndTouchAsync<string>(key, TimeSpan.FromSeconds(2));
            Console.WriteLine("[{0:HH:mm:ss.fff}] Got: '{1}', Status: {2}", DateTime.Now, result3.Value, result3.Status);

            // Waiting 4 seconds again
            await Task.Delay(4000);

            var result4 = await _bucket.GetAsync<string>(key);
            Console.WriteLine("[{0:HH:mm:ss.fff}] Got: '{1}', Status: {2}", DateTime.Now, result4.Value, result4.Status);


            // Creating an item without expiration
            await _bucket.UpsertAsync(key, "Hello world!");

            // Updating the TTL with Touch
            var result5 = await _bucket.TouchAsync(key, TimeSpan.FromSeconds(2));
            Console.WriteLine("[{0:HH:mm:ss.fff}] Got: '{1}', Status: {2}", DateTime.Now, "N/A", result5.Status);

            // Waiting 4 seconds yet again
            await Task.Delay(4000);

            var result6 = await _bucket.GetAsync<string>(key);
            Console.WriteLine("[{0:HH:mm:ss.fff}] Got: '{1}', Status: {2}", DateTime.Now, result6.Value, result6.Status);
        }

        static void Main(string[] args)
        {
            new Expiration().ExecuteAsync().Wait();
        }
    }
}
