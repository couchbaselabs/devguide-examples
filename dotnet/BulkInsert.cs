using Couchbase;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DevGuide
{
    public class BulkInsert : ConnectionBase
    {
        public override async Task ExecuteAsync()
        {
            // Create 100 Data objects
            var data = Enumerable.Range(1, 100).Select(i => new Data { Number = i });
            
            // Option 1a: use Upsert with an IDictionary<string, object> of documents in upsert
            var bulkData = data.ToDictionary(d => "dotnetDevguideExample-" + d.Number);
            // Note: There is no UpsertAsync overload that takes multiple values (yet)
            // which is why this example wraps the synchronized method in a new Task.
            // If your code is fully synchronous, you can simply call _bucket.Upsert(...)
            var bulkResult = await Task.Run(() => _bucket.Upsert(bulkData));

            var successCount = bulkResult.Where(r => r.Value.Success).Count();
            Console.WriteLine("Upserted {0} values", bulkResult.Count);

            // Option 1b: Specify ParallelOptions to customize how the client parallelizes the upserts
            var parallelOptions = new ParallelOptions { MaxDegreeOfParallelism = 32 };
            var bulkResult2 = await Task.Run(() => _bucket.Upsert(bulkData, parallelOptions));

            successCount = bulkResult2.Where(r => r.Value.Success).Count();
            Console.WriteLine("Upserted {0} values", successCount);

            // Option 2: Spawn multiple Upsert tasks and wait on for all to complete
            var bulkTasks = bulkData.Select(d => _bucket.UpsertAsync(d.Key, d.Value));
            var bulkResults = await Task.WhenAll(bulkTasks);

            successCount = bulkResults.Where(r => r.Success).Count();
            Console.WriteLine("Upserted {0} values", successCount);
        }

        static void Main(string[] args)
        {
            new BulkInsert().ExecuteAsync().Wait();
        }
    }
}
