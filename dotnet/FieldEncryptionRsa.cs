using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using System.Threading.Tasks;
using Couchbase.Configuration.Client;
using Couchbase.Extensions.Encryption;
using Couchbase.Extensions.Encryption.Providers;
using Couchbase.Extensions.Encryption.Stores;
using Newtonsoft.Json;

namespace DevGuide
{
    /*********************************PLEASE READ!***********************************
     * In order to run this example, you will need to generate an X509 certificate in
     * the .pfx (Personal Information Exchange Format) and then change it's 'Copy to
     * to Output Directory' flag to 'Copy Always' in the Properties Window in VS. To do
     * this make sure you have OpenSsl installed: https://wiki.openssl.org/index.php/Binaries.
     * Then open open a command prompt and do the following steps:
     *
     * Step 1 - Generate an RSA private key:
     *
     *          openssl genrsa -out private.key 2048
     *
     * Step 2 - Optionally remove the password:
     *
     *          openssl rsa -in private.key -out private-nokey.pem
     *
     * Step 3 - Generate a new x509 certificate from the private key.  You will have to enter quite a bit of
     *          information (country, state, company, etc):
     *
     *          openssl req -new -x509 -key private.key -out publickey.cer -days 365
     *
     * Step 4 - Export the private key and the x509 certificate into a .pfx using 'password' as the password:
     *
     *          openssl pkcs12 -export -out public_privatekey.pfx -inkey private.key -in publickey.cer
     *
     * Once you have done step 4, copy the public_privatekey.pfx into the DevGuide project and change its "Copy
     * To Output Directory" flag to "Copy Always".
     */
    public class FieldEncryptionRsa : ConnectionBase
    {
        public override async Task ExecuteAsync()
        {
            //The Password field will be encrypted - see the definition of the
            //People class below for reference of how to annotate the property
            var teddy = new Person
            {
                Age = 33,
                FirstName = "Ted",
                LastName = "DeBloss",
                Password = "ssloBeD12345"
            };

            //Password field will be encrypted in transport and at rest in the database
            var insert = await _bucket.UpsertAsync("person::1", teddy).ConfigureAwait(false);
            if (insert.Success)
            {
                Console.WriteLine("Inserted encrypted Person.Password..." + Environment.NewLine);
            }

            //If the document is fetched as a string or dynamic it will by-pass decryption so we
            //can see how the document is stored within Couchbase without triggering decryption
            var encrypted = await _bucket.GetAsync<dynamic>("person::1").ConfigureAwait(false);
            Console.WriteLine(JsonConvert.SerializeObject(encrypted.Value, Formatting.Indented));

            //Fetching the document will reverse the encryption process so Password at the
            //application only will be in plaintext. In transport and in storage it will encrypted.
            var get = await _bucket.GetAsync<Person>("person::1").ConfigureAwait(false);
            if (get.Success)
            {
                Console.WriteLine(Environment.NewLine + "Fetched decrypted Person.Password...");
                Console.WriteLine(get.Value);
            }

            Console.Read();
        }

        protected override ClientConfiguration GetConnectionConfig()
        {
            //Define some common key names for associating keys
            const string publicKeyName = "MyPublicKeyName";
            const string privateKeyName = "MyPrivateKeyName";

            //Open the X509 cert from the .pfx generated from the steps above
            var cert = new X509Certificate2("public_privatekey.pfx", "password",
                X509KeyStorageFlags.MachineKeySet | X509KeyStorageFlags.Exportable);

            //Create the keystore for X509 certs
            var keyStore = new X509CertificateKeyStore(cert)
            {
                PrivateKeyName = privateKeyName,
                PublicKeyName = publicKeyName
            };

            //Create the RSA crypto provider
            var cryptoProvider = new RsaCryptoProvider(keyStore)
            {
                PrivateKeyName = privateKeyName,
                PublicKeyName = publicKeyName
            };

            //Get the config and enable field encryption
            var config = base.GetConnectionConfig();
            config.EnableFieldEncryption("MyRSAProvider", cryptoProvider);
            return config;
        }

        private class Person
        {
            //Annotate the field to be encrypted
            [EncryptedField(Provider = "MyRSAProvider")]
            public string Password { get; set; }

            //The rest will be transported and stored unencrypted
            public string FirstName { get; set; }
            public string LastName { get; set; }
            public string UserName { get; set; }
            public int Age { get; set; }

            public override string ToString()
            {
                return JsonConvert.SerializeObject(this, Formatting.Indented);
            }
        }

        static void Main(string[] args)
        {
            new FieldEncryptionRsa().ExecuteAsync().Wait();
        }
    }
}
