using Couchbase;
using Couchbase.Configuration.Client;
using Couchbase.Core;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace DevGuide
{
    /// <summary>
    /// For an example of configuring the Couchbase connection through App.config/Web.config
    /// see the ConnectionConfig class
    /// </summary>
    public class ConnectionBase
    {
        protected ICluster _cluster;
        protected IBucket _bucket;

        public ConnectionBase()
        {
            Connect();
        }

        private void Connect()
        {
            var config = GetConnectionConfig();

            _cluster = new Cluster(config);
            _cluster.Authenticate("Administrator", "password");

            _bucket = _cluster.OpenBucket();
        }

        protected virtual ClientConfiguration GetConnectionConfig()
        {
            return new ClientConfiguration
            {
                Servers = new List<Uri> {
                    new Uri("http://localhost:8091/pools")
                },
                BucketConfigs = new Dictionary<string, BucketConfiguration>
                  {
                    { "default", new BucketConfiguration
                    {
                      BucketName = "default",
                      UseSsl = false,
                      Password = "",
                      DefaultOperationLifespan = 2000,
                      PoolConfiguration = new PoolConfiguration
                      {
                        MaxSize = 10,
                        MinSize = 5,
                        SendTimeout = 12000
                      }
                    }}
                  }
            };
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
            new ConnectionBase().ExecuteAsync().Wait();
        }
    }
}
