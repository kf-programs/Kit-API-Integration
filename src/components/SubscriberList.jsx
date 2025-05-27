import { useState } from 'react';
import axios from 'axios';

function SubscriberList() {
  const [apiKey, setApiKey] = useState('');
  const [subscribers, setSubscribers] = useState([]);
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const fetchSubscribers = async () => {
    setIsLoading(true);
    try {
      const response = await axios.post('/api/subscribers', null, {
        headers: {
          'Kit-Api-Key': apiKey,
        },
      });
      setSubscribers(response.data);
      setError('');
    } catch (err) {
      setError('Failed to fetch subscribers: ' + err.message);
      setSubscribers([]);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div>
      <h1>Kit API Subscriber List</h1>
      <div style={{ margin: '20px 0' }}>
        <input
          type="text"
          placeholder="Enter Kit API Key"
          value={apiKey}
          onChange={(e) => setApiKey(e.target.value)}
          style={{ padding: '8px', marginRight: '10px' }}
        />
        <button 
          onClick={fetchSubscribers}
          disabled={isLoading}
          style={{ padding: '8px 16px' }}
        >
          {isLoading ? 'Loading...' : 'Fetch Subscribers'}
        </button>
      </div>
      
      {error && <p style={{ color: 'red' }}>{error}</p>}
      
      {subscribers.length > 0 && (
        <div style={{ maxWidth: '800px', margin: '0 auto' }}>
          <h2>Total Subscribers: {subscribers.length}</h2>
          <div style={{ maxHeight: '400px', overflow: 'auto', border: '1px solid #ccc' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr>
                  <th style={{ padding: '10px', backgroundColor: '#f5f5f5' }}>Email Address</th>
                </tr>
              </thead>
              <tbody>
                {subscribers.map((email, index) => (
                  <tr key={index} style={{ borderBottom: '1px solid #eee' }}>
                    <td style={{ padding: '8px' }}>{email}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}

export default SubscriberList;
