import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import './App.css';
import SubscriberList from './components/SubscriberList';
import TagSubscribers from './components/TagSubscribers';

function App() {
  return (
    <BrowserRouter>
      <div className="App">
        <nav style={{ margin: '20px 0', padding: '10px', backgroundColor: '#f5f5f5' }}>
          <Link to="/" style={{ marginRight: '20px' }}>View Subscribers</Link>
          <Link to="/tag">Tag Subscribers</Link>
        </nav>
        <Routes>
          <Route path="/" element={<SubscriberList />} />
          <Route path="/tag" element={<TagSubscribers />} />
        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;
