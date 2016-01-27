using Couchbase;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DevGuide
{
    public class Counter : ConnectionBase
    {
        public override async Task ExecuteAsync()
        {
            var key = "dotnetDevguideExampleCounter-" + DateTime.Now.Ticks;

            // Try to increment a counter that doesn't exist. 
            // This will create the counter with an initial value of 1 regardless of delta specified
            var counter = await _bucket.IncrementAsync(key, 10);
            Console.WriteLine("Initial value = N/A, Increment = 10, Counter value: " + counter.Value);

            // Remove the counter so we can try again
            await _bucket.RemoveAsync(key);
            Console.WriteLine("Trying again.");

            // Create a counter with an initial value of 13. Again, delta is ignored in this case.
            var counter2 = await _bucket.IncrementAsync(key, 10, 13);
            Console.WriteLine("Initial value = 13, Increment = 10, Counter value: " + counter2.Value);

            // Increment the counter by 10. If the counter exists, the inital value is ignored.
            var counter3 = await _bucket.IncrementAsync(key, 10, 13);
            Console.WriteLine("Initial value = 13, Increment = 10, Counter value: " + counter3.Value);

            // Decrement the counter by 20.
            var counter4 = await _bucket.DecrementAsync(key, 20, 13);
            Console.WriteLine("Initial value = 13, Decrement = 20, Counter value: " + counter4.Value);
        }

        static void Main(string[] args)
        {
            new Counter().ExecuteAsync().Wait();
        }
    }
}
