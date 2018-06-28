using System.Threading.Tasks;
using Couchbase;
using Couchbase.Configuration.Client;

namespace DevGuide
{
    /// <summary>
    /// This example shows how to use x509 certificates for authentication when connecting to Couchbase.
    /// Before running this example, the following scripts must be run generate the x509 certs and configure Couchbase:
    ///
    ///     1 - https://github.com/couchbaselabs/devguide-examples/blob/master/etc/x509-cert/setup-x509-on-cluster.sh
    ///     2 - https://github.com/couchbaselabs/devguide-examples/blob/master/etc/x509-cert/generate-new-client-cert.sh
    ///     3 - https://github.com/couchbaselabs/devguide-examples/blob/master/etc/x509-cert/create-pfx-for-net.sh
    ///
    /// Assuming you are using Windows, once you have run the last script, copy the client.pfx to the machine that you are
    /// running this application on. If you are using OSX or Linux, you may have to omit or add some steps to get it to work
    /// correctly.
    /// </summary>
    // ReSharper disable once InconsistentNaming
    public class x509CertAuthenication : ConnectionBase
    {
        public override async Task ExecuteAsync()
        {
            var pathToPfx = "client.pfx"; //this can be a fully qualified path to the .pfx you created by running the scripts above
            var password = "password"; //this can be whatever password you entered while creating the .pfx by running the scripts above.

            var config = GetConnectionConfig();

            //this is required if you x509 authentication
            config.EnableCertificateAuthentication = true;

            _cluster = new Cluster(config);

            //Configure the cluster to use the CertAuthenticator
            //_cluster.Authenticate(new CertAuthenticator(pathToPfx, password));

            _bucket = _cluster.OpenBucket("travel-sample");

            var result = _bucket.Upsert("mykey", "myvalue");
        }


        static void Main(string[] args)
        {
            new x509CertAuthenication().ExecuteAsync().Wait();
        }
    }
}
