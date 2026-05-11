import { Box, TextField, InputAdornment } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';

const SearchBar = ({
                       searchTerm, setSearchTerm,
                       minPrice, setMinPrice,
                       maxPrice, setMaxPrice,
                       startDate, setStartDate,
                       endDate, setEndDate
                   }) => {

    // Validación para no permitir números negativos
    const handlePriceChange = (setter) => (e) => {
        const value = e.target.value;
        if (value === '' || Number(value) >= 0) setter(value);
    };

    return (
        <Box sx={{
            backgroundColor: 'white',
            p: 3,
            borderRadius: '16px',
            mb: 4,
            boxShadow: '0px 4px 20px rgba(0,0,0,0.05)',
            display: 'flex',
            gap: 2,
            flexWrap: 'wrap'
        }}>
            {/* Buscador de texto principal */}
            <TextField
                sx={{ flexGrow: 2, minWidth: '200px' }}
                placeholder="¿A dónde quieres ir?"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                InputProps={{
                    startAdornment: <InputAdornment position="start"><SearchIcon /></InputAdornment>,
                }}
            />

            {/* Rango de Precios */}
            <TextField
                label="Precio Mín"
                type="number"
                sx={{ width: '130px' }}
                value={minPrice}
                onChange={handlePriceChange(setMinPrice)}
                inputProps={{ min: 0 }}
            />
            <TextField
                label="Precio Máx"
                type="number"
                sx={{ width: '130px' }}
                value={maxPrice}
                onChange={handlePriceChange(setMaxPrice)}
                inputProps={{ min: 0 }}
            />

            {/* Rango de Fechas */}
            <TextField
                label="Desde"
                type="date"
                sx={{ width: '160px' }}
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                InputLabelProps={{ shrink: true }}
            />
            <TextField
                label="Hasta"
                type="date"
                sx={{ width: '160px' }}
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                InputLabelProps={{ shrink: true }}
            />
        </Box>
    );
};

export default SearchBar;