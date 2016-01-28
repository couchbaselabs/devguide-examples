using Couchbase;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DevGuide
{
    public class Retrieve : ConnectionBase
    {
        public override async Task ExecuteAsync()
        {
            var key = "dotnetDevguideExampleRetrieve-" + DateTime.Now.Ticks;
            var data = new Data
            {
                Number = 42,
                Text = "Life, the Universe, and Everything",
                Date = DateTime.UtcNow
            };

            // Get non-existent document. 
            // Note that it's enough to check the Status property,
            // We're only checking all three to show they exist.
            var notFound = await _bucket.GetAsync<dynamic>(key);
            if (!notFound.Success &&
                notFound.Status == Couchbase.IO.ResponseStatus.KeyNotFound &&
                notFound.Value == null)
                Console.WriteLine("Document doesn't exist!");


            // Prepare a string value
            await _bucket.UpsertAsync(key, "Hello Couchbase!");

            // Get a string value
            var nonDocResult = await _bucket.GetAsync<string>(key);
            Console.WriteLine("Found: " + nonDocResult.Value);

            // Prepare a JSON document value
            await _bucket.UpsertAsync(key, data);

            // Get a JSON document string value
            var docResult = await _bucket.GetAsync<Data>(key);
            Console.WriteLine("Found: " + docResult.Value);
        }

        static void Main(string[] args)
        {
            new Retrieve().ExecuteAsync().Wait();
        }
    }
}
