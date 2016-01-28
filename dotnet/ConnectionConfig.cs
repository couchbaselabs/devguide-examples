using Couchbase;
using Couchbase.Core;
using System;
using System.Threading.Tasks;

namespace DevGuide
{
    public class ConnectionConfig
    {
        protected ICluster _cluster;
        protected IBucket _bucket;

        public ConnectionConfig()
        {
            Connect();
        }

        private void Connect()
        {
            _cluster = new Cluster("couchbaseClients/couchbase");
            _bucket = _cluster.OpenBucket();
        }

        private void Disconnect()
        {
            _cluster.CloseBucket(_bucket);
            _bucket.Dispose();
            _bucket = null;
            _cluster.Dispose();
            _cluster = null;
        }

        public virtual async Task ExecuteAsync()
        {
            Console.WriteLine("Connected to bucket '{0}'", _bucket.Name);
        }

        static void Main(string[] args)
        {
            new ConnectionConfig().ExecuteAsync().Wait();
        }

    }
}
