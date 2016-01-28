using Couchbase;
using Couchbase.IO;
using System;
using System.Threading.Tasks;

namespace DevGuide
{
    public class Durability : ConnectionBase
    {
        public override async Task ExecuteAsync()
        {
            var key = "dotnetDevguideExampleDurability-" + DateTime.Now.Ticks;
            var data = new Data
            {
                Number = 42,
                Text = "Life, the Universe, and Everything",
                Date = DateTime.UtcNow
            };
            
            // The ReplicateTo parameter must be less than or equal to the number of replicas 
            // you have configured. Assuming that 3 replicas are configured, the following call
            // waits for replication to 3 replicas and persistence to 4 nodes in total.
            var result = await _bucket.UpsertAsync(key, data, ReplicateTo.Three, PersistTo.Four);
            Console.WriteLine("Durability status: " + result.Durability);

            if(!result.Success)
            {
                if (result.Status == ResponseStatus.NoReplicasFound)
                    Console.WriteLine("Write failed - not enough replicas configured to satisfy durability requirements");
                else
                    Console.WriteLine("An error has occured: {0}\r\n{1}", result.Message, result.Exception.ToString());
            }
            else
            {
                // It's possible for a write to succeed, but not satisfy durability.
                // For example, writing with PersistTo.Two and ReplicateTo.Zero on a 1-node cluster.
                if (result.Durability == Couchbase.IO.Operations.Durability.NotSatisfied)
                    Console.WriteLine("Write succeeded, but some durability requirements were not satisfied.");
            }

            // Wait for the write to be persisted to disk on one (normally the master) node.
            var result2 = await _bucket.UpsertAsync(key, data, ReplicateTo.Zero, PersistTo.One);
            Console.WriteLine("Durability status: " + result.Durability);
        }

        static void Main(string[] args)
        {
            new Durability ().ExecuteAsync().Wait();
        }
    }
}
