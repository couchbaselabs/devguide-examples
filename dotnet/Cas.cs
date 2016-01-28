using Couchbase;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DevGuide
{
    public class Cas : ConnectionBase
    {
        public override async Task ExecuteAsync()
        {
            var key = "dotnetDevguideExampleCas-" + DateTime.Now.Ticks;
            var data = new Data
            {
                Number = 0
            };

            // Set the inital number value to 0
            await _bucket.UpsertAsync(key, data);

            // Try to increment the number 1000 times without using CAS (10 threads x 100 increments)
            // We would expect the result to be Number == 1000 at the end of the process.
            var tasksWithoutCas = Enumerable.Range(1, 10).Select(i => UpdateNumberWithoutCas(key, 100));
            await Task.WhenAll(tasksWithoutCas);

            // Check if the actual result is 1000 as expected
            var result = await _bucket.GetAsync<Data>(key);
            Console.WriteLine("Expected number = 1000, actual number = " + result.Value.Number);


            // Set the inital number value back to 0
            await _bucket.UpsertAsync(key, data);

            // Now try to increment the number 1000 times with CAS
            var tasksWithCas = Enumerable.Range(1, 10).Select(i => UpdateNumberWithCas(key, 100));
            await Task.WhenAll(tasksWithCas);

            // Check if the actual result is 1000 as expected
            var result2 = await _bucket.GetAsync<Data>(key);
            Console.WriteLine("Expected number = 1000, actual number = " + result2.Value.Number);
        }

        private Task UpdateNumberWithoutCas(string key, int count)
        {
            return Task.Run(async () =>
            {
                for (int i = 0; i < count; i++)
                {
                    // Get the document
                    var result = await _bucket.GetAsync<Data>(key);
                    // Update the document
                    result.Value.Number++;
                    // Store the document back without CAS
                    await _bucket.ReplaceAsync(key, result.Value);
                }
            });
        }

        private Task UpdateNumberWithCas(string key, int count)
        {
            return Task.Run(async () =>
            {
                for (int i = 0; i < count; i++)
                {
                    IOperationResult<Data> result = null;
                    var retries = 100;
                    do
                    {
                        // Get the document
                        result = await _bucket.GetAsync<Data>(key);
                        // Update the document
                        result.Value.Number++;
                        // Store the document back without CAS
                        result = await _bucket.ReplaceAsync(key, result.Value, result.Cas);
                    }
                    while (result != null && 
                           !result.Success && 
                           // The .NET SDK returns a KeyExists status when the provided CAS doesn't match
                           result.Status == Couchbase.IO.ResponseStatus.KeyExists &&
                           retries-- > 0);
                }
            });
        }

        static void Main(string[] args)
        {
            new Cas().ExecuteAsync().Wait();
        }
    }
}
