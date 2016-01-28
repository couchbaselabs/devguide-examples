using Couchbase;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DevGuide
{
    public class BulkGet : ConnectionBase
    {
        public override async Task ExecuteAsync()
        {
            // Call BulkInsert to generate some data
            await new BulkInsert().ExecuteAsync();

            // Generate the keys to retrieve
            var keys = Enumerable.Range(1, 100).Select(i => "dotnetDevguideExample-" + i).ToList();


            // Option 1a: use Get with a IList<string> of document keys to retrieve
            // Note: There is no GetAsync overload that takes multiple keys (yet)
            // which is why this example wraps the synchronized method in a new Task.
            // If your code is fully synchronous, you can simply call _bucket.Get(...)
            var bulkResult = await Task.Run(() => _bucket.Get<Data>(keys));

            var successCount = bulkResult.Where(r => r.Value.Success).Count();
            Console.WriteLine("Got {0} values", bulkResult.Count);

            // Option 1b: Specify ParallelOptions to customize how the client parallelizes the gets
            var parallelOptions = new ParallelOptions { MaxDegreeOfParallelism = 32 };
            var bulkResult2 = await Task.Run(() => _bucket.Get<Data>(keys, parallelOptions));

            successCount = bulkResult2.Where(r => r.Value.Success).Count();
            Console.WriteLine("Got {0} values", successCount);

            // Option 2: Spawn multiple Upsert tasks and wait on for all to complete
            var bulkTasks = keys.Select(key => _bucket.GetAsync<Data>(key));
            var bulkResults = await Task.WhenAll(bulkTasks);

            successCount = bulkResults.Where(r => r.Success).Count();
            Console.WriteLine("Got {0} values", successCount);

        }

        static void Main(string[] args)
        {
            new BulkGet().ExecuteAsync().Wait();
        }
    }
}
