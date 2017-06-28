package contracts.authserver;

import org.springframework.cloud.contract.spec.Contract

Contract.make {
  request {
	method 'GET'
	url '/api/me'
	headers {
	  header('Authorization', execute('oauth()'))
	}
  }
response {
  status 200
  body('''{
  "authorities": [
    {
      "authority": "ROLE_USER"
    },
    {
      "authority": "ROLE_ACTUATOR"
    }
  ],
  "details": {
    "remoteAddress": "127.0.0.1",
    "sessionId": null,
    "tokenValue": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsiYWNjb3VudHNlcnZpY2UiXSwidXNlcl9uYW1lIjoidXNlciIsInNjb3BlIjpbInJlYWQiLCJ3cml0ZSJdLCJleHAiOjE0OTg2MjAwOTUsImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiIsIlJPTEVfQUNUVUFUT1IiXSwianRpIjoiYTUxMzk1ZTMtMmZjYi00MTc0LTlmOTEtZTYwMmU5NzRjZmUzIiwiY2xpZW50X2lkIjoiY2xpZW50In0.r7ZxON6wWi3K5sJhhpLcR3kKDkFJ1AB69JMGVnO985I",
    "tokenType": "Bearer",
    "decodedDetails": null
  },
  "authenticated": true,
  "userAuthentication": {
    "authorities": [
      {
        "authority": "ROLE_USER"
      },
      {
        "authority": "ROLE_ACTUATOR"
      }
    ],
    "details": null,
    "authenticated": true,
    "principal": "user",
    "credentials": "N/A",
    "name": "user"
  },
  "principal": "user",
  "oauth2Request": {
    "clientId": "client",
    "scope": [
      "read",
      "write"
    ],
    "requestParameters": {
      "client_id": "client"
    },
    "resourceIds": [
      "accountservice"
    ],
    "authorities": [
      
    ],
    "approved": true,
    "refresh": false,
    "redirectUri": null,
    "responseTypes": [
      
    ],
    "extensions": {
      
    },
    "grantType": null,
    "refreshTokenRequest": null
  },
  "clientOnly": false,
  "credentials": "",
  "name": "user"
}''')
  		testMatchers {
			  jsonPath('$.authorities', byType {
				minOccurrence(1) // assert has min size 1
			  })
			  jsonPath('$.details.remoteAddress', byRegex(/\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}/))
			  jsonPath('$.[?(@["credentials"] == "")].credentials', byType{} )
			  jsonPath('$.details.sessionId', byCommand('assertIsNull($it)'))
			  jsonPath('$.details.tokenValue', byType{})
			  jsonPath('$.userAuthentication.authorities', byType {
				minOccurrence(1) // assert has min size 1
			  })
			  jsonPath('$.userAuthentication.details', byCommand('assertIsNull($it)') )
			  jsonPath('$.userAuthentication.principal', byType{} )
			  jsonPath('$.userAuthentication.name', byType{} )
			  jsonPath('$.principal', byType{})
			  jsonPath('$.oauth2Request.clientId', byType{ })
			  jsonPath('$.oauth2Request.scope', byType{ minOccurrence(1) })
			  jsonPath('$.oauth2Request.resourceIds', byType{ minOccurrence(1) })
			  jsonPath('$.oauth2Request.grantType', byCommand('assertIsNull($it)'))
		}
		headers {
			contentType(applicationJson())
		}
 }
}