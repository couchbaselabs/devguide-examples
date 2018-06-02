using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Couchbase.Configuration.Client;
using Couchbase.Extensions.Encryption;
using Couchbase.Extensions.Encryption.Providers;
using Couchbase.Extensions.Encryption.Stores;
using Newtonsoft.Json;

namespace DevGuide
{
    public class FieldEncryptionAes : ConnectionBase
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

        static void Main(string[] args)
        {
            new FieldEncryptionAes().ExecuteAsync().Wait();
        }

        protected override ClientConfiguration GetConnectionConfig()
        {
            //for encryption we need a key
            const string publicKey = "!mysecretkey#9^5usdk39d&dlf)03sL";
            const string publicKeyName = "publickey";

            //for authentication we need a password
            const string signingKey = "myauthpassword";
            const string signingKeyName = "mysecret";

            //An in-memory insecure key store - for real world applications use FileSystemKeyStore
            //which uses DAPI to protect the keys that are stotred within it.
            var keystore = new InsecureKeyStore(
                new KeyValuePair<string, string>(publicKeyName, publicKey),
                new KeyValuePair<string, string>(signingKeyName, signingKey));

            //This example is using the symmetric key AES algorithm
            var cryptoProvider = new AesCryptoProvider(keystore)
            {
                PublicKeyName = publicKeyName,
                SigningKeyName = signingKeyName
            };

            //Get the config and enable field encryption
            var config = base.GetConnectionConfig();
            config.EnableFieldEncryption("MyAESProvider", cryptoProvider);
            return config;
        }

        private class Person
        {
            //Annotate the field to be encrypted
            [EncryptedField(Provider = "MyAESProvider")]
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
    }
}
