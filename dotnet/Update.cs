using Couchbase;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DevGuide
{
    public class Update : ConnectionBase
    {
        public override async Task ExecuteAsync()
        {
            var key = "dotnetDevguideExampleUpdate-" + DateTime.Now.Ticks;
            var data = new Data
            {
                Number = 42,
                Text = "Life, the Universe, and Everything",
                Date = DateTime.UtcNow
            };

            // Prepare the document
            // Note that upsert works whether the document exists or not
            await _bucket.UpsertAsync(key, data);

            // Change the data
            data.Number++;
            data.Text = "What's 7 * 6 + 1?";
            data.Date = DateTime.UtcNow;


            // Try to insert under the same key should fail
            var insertResult = await _bucket.InsertAsync(key, data);
            if (!insertResult.Success)
                Console.WriteLine("Inserting under an existing key fails as expected.");


            // Replace existing document
            // Note this only works if the key already exists
            var replaceResult = await _bucket.ReplaceAsync(key, data);


            // Check that the data was updated
            var newDocument = await _bucket.GetAsync<Data>(key);
            Console.WriteLine("Got: " + data.Text);
        }

        static void Main(string[] args)
        {
            new Update().ExecuteAsync().Wait();
        }
    }
}
